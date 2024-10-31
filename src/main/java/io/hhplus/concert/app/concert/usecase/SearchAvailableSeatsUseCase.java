package io.hhplus.concert.app.concert.usecase;

import io.hhplus.concert.app.concert.domain.ConcertSeat;
import io.hhplus.concert.app.concert.port.ConcertPort;
import io.hhplus.concert.app.concert.port.ConcertSchedulePort;
import io.hhplus.concert.app.concert.port.ConcertSeatPort;
import io.hhplus.concert.app.concert.usecase.dto.ConcertSeatResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@AllArgsConstructor
@Service
public class SearchAvailableSeatsUseCase {

    private final ConcertPort concertPort;
    private final ConcertSchedulePort concertSchedulePort;
    private final ConcertSeatPort concertSeatPort;

    public record Input(
            Long concertId,
            Long concertScheduleId
    ) { }

    public record Output(
            List<ConcertSeatResult> seatResults
    ) { }

    @Transactional
    public Output execute(Input input) {

        if (!concertPort.existsById(input.concertId())) {
            throw new NoSuchElementException("Concert not found: " + input.concertId());
        }

        if (!concertSchedulePort.existsById(input.concertScheduleId())) {
            throw new NoSuchElementException("Concert Schedule not found: " + input.concertScheduleId());
        }

        // ** 원래는 낙관락 사용해야 하는 부분.
        List<ConcertSeat> seats = concertSeatPort.getAllByConcertScheduleIdWithLock(input.concertScheduleId());

        return new Output(
                seats.stream().map(
                        seat -> new ConcertSeatResult(
                                input.concertId(),
                                seat.getConcertScheduleId(),
                                seat.getId(),
                                seat.getLabel(),
                                seat.getSeatNumber(),
                                seat.getIsActive(),
                                seat.getPrice()
                        )
                ).toList()
        );
    }
}
