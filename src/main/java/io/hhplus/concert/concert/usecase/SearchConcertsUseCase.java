package io.hhplus.concert.concert.usecase;

import io.hhplus.concert.concert.domain.Concert;
import io.hhplus.concert.concert.port.ConcertPort;
import io.hhplus.concert.concert.usecase.dto.ConcertResult;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class SearchConcertsUseCase {

    private final ConcertPort concertPort;

    public record Input(
            Pageable pageable
    ) { }

    public record Output(
            Page<ConcertResult> concertResultPage
    ) {}

    public Output execute(Input input) {
        Page<Concert> concertPage = concertPort.findAllByPageable(input.pageable());
        return new Output(
                concertPage.map(concert -> new ConcertResult(
                        concert.getId(),
                        concert.getTitle(),
                        concert.getCast()
                ))
        );
    }
}
