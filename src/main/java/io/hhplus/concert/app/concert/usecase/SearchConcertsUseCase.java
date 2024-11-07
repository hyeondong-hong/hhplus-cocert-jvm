package io.hhplus.concert.app.concert.usecase;

import io.hhplus.concert.app.concert.domain.Concert;
import io.hhplus.concert.app.concert.port.ConcertItemsRedisPort;
import io.hhplus.concert.app.concert.port.ConcertPort;
import io.hhplus.concert.app.concert.usecase.dto.ConcertResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class SearchConcertsUseCase {

    private final ConcertPort concertPort;

    private final ConcertItemsRedisPort concertItemsRedisPort;

    public record Input(
            Pageable pageable
    ) { }

    public record Output(
            Page<ConcertResult> concertResultPage
    ) { }

    public Output execute(Input input) {
        Page<Concert> concertPage = concertItemsRedisPort.getConcerts(input.pageable());
        if (concertPage == null) {
            concertPage = concertPort.findAllByPageable(input.pageable());
            concertItemsRedisPort.setConcerts(input.pageable(), concertPage);
        }
        return new Output(
                concertPage.map(concert -> new ConcertResult(
                        concert.getId(),
                        concert.getTitle(),
                        concert.getCast()
                ))
        );
    }
}
