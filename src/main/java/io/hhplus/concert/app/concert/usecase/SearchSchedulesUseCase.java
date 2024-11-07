package io.hhplus.concert.app.concert.usecase;

import io.hhplus.concert.app.concert.domain.ConcertSchedule;
import io.hhplus.concert.app.concert.port.ConcertPort;
import io.hhplus.concert.app.concert.port.ConcertSchedulePort;
import io.hhplus.concert.app.concert.usecase.dto.ConcertScheduleResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class SearchSchedulesUseCase {

    private final ConcertPort concertPort;
    private final ConcertSchedulePort concertSchedulePort;

    public record Input(
            Long concertId,
            Pageable pageable
    ) { }

    public record Output(
            Page<ConcertScheduleResult> concertScheduleResultPage
    ) {}

    public Output execute(Input input) {

        concertPort.existsOrThrow(input.concertId());

        Page<ConcertSchedule> concertPage = concertSchedulePort.findAllByConcertIdAndPageable(
                input.concertId(),
                input.pageable()
        );
        return new Output(
                concertPage.map(concert -> new ConcertScheduleResult(
                        concert.getConcertId(),
                        concert.getId(),
                        concert.getScheduledAt()
                ))
        );
    }
}
