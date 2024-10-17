package io.hhplus.concert.concert.usecase;

import io.hhplus.concert.concert.domain.ConcertSchedule;
import io.hhplus.concert.concert.port.ConcertPort;
import io.hhplus.concert.concert.port.ConcertSchedulePort;
import io.hhplus.concert.concert.usecase.dto.ConcertScheduleResult;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

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

        if (!concertPort.existsById(input.concertId())) {
            throw new NoSuchElementException("Concert not found: " + input.concertId());
        }

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
