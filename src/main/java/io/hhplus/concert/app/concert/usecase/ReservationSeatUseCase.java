package io.hhplus.concert.app.concert.usecase;

import io.hhplus.concert.app.concert.domain.ConcertSeat;
import io.hhplus.concert.app.concert.domain.Reservation;
import io.hhplus.concert.app.concert.domain.enm.ReservationStatus;
import io.hhplus.concert.app.concert.domain.event.ReservationSeatEvent;
import io.hhplus.concert.app.concert.port.ConcertPort;
import io.hhplus.concert.app.concert.port.ConcertSchedulePort;
import io.hhplus.concert.app.concert.port.ConcertSeatPort;
import io.hhplus.concert.app.concert.port.ReservationPort;
import io.hhplus.concert.app.concert.port.publisher.ReservationSeatPublisher;
import io.hhplus.concert.app.concert.usecase.dto.ReservationResult;
import io.hhplus.concert.app.payment.domain.Payment;
import io.hhplus.concert.app.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.app.payment.port.PaymentPort;
import io.hhplus.concert.app.user.domain.Token;
import io.hhplus.concert.app.user.port.TokenPort;
import io.hhplus.concert.config.aop.annotation.RedisLock;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReservationSeatUseCase {

    private final ConcertPort concertPort;
    private final ConcertSchedulePort concertSchedulePort;
    private final ConcertSeatPort concertSeatPort;
    private final TokenPort tokenPort;

    private final ReservationSeatPublisher reservationSeatPublisher;

    public record Input(
            String keyUuid,
            Long concertId,
            Long concertScheduleId,
            Long concertSeatId
    ) { }

    public record Output(
            String message
    ) { }

//    @RedisLock(transactional = true, key = "Reservation", dtoName = "input", fields = {"concertSeatId"})
    public Output execute(Input input) {

        Token token = tokenPort.getByKey(input.keyUuid());

        concertPort.existsOrThrow(input.concertId());
        concertSchedulePort.existsOrThrow(input.concertScheduleId());
        concertSeatPort.existsOrThrow(input.concertSeatId());

        reservationSeatPublisher.publish(
                new ReservationSeatEvent(
                        token.getUserId(),
                        input.concertSeatId()
                )
        );

        return new Output("Request completed.");
    }
}
