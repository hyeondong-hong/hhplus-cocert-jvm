package io.hhplus.concert.app.concert.usecase;

import io.hhplus.concert.app.concert.domain.ConcertSeat;
import io.hhplus.concert.app.concert.domain.Reservation;
import io.hhplus.concert.app.concert.domain.enm.ReservationStatus;
import io.hhplus.concert.app.concert.port.ConcertPort;
import io.hhplus.concert.app.concert.port.ConcertSchedulePort;
import io.hhplus.concert.app.concert.port.ConcertSeatPort;
import io.hhplus.concert.app.concert.port.ReservationPort;
import io.hhplus.concert.app.concert.usecase.dto.ReservationResult;
import io.hhplus.concert.app.payment.domain.Payment;
import io.hhplus.concert.app.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.app.payment.port.PaymentPort;
import io.hhplus.concert.app.user.domain.Token;
import io.hhplus.concert.app.user.port.TokenPort;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Slf4j
@AllArgsConstructor
@Service
public class ReservationSeatUseCase {

    private final ConcertPort concertPort;
    private final ConcertSchedulePort concertSchedulePort;
    private final ConcertSeatPort concertSeatPort;
    private final ReservationPort reservationPort;
    private final PaymentPort paymentPort;
    private final TokenPort tokenPort;

    public record Input(
            String keyUuid,
            Long concertId,
            Long concertScheduleId,
            Long concertSeatId
    ) { }

    public record Output(
            ReservationResult reservationResult
    ) { }

    @Transactional
    public Output execute(Input input) {

        Token token = tokenPort.getByKey(input.keyUuid());

        concertPort.existsOrThrow(input.concertId());
        concertSchedulePort.existsOrThrow(input.concertScheduleId());

        ConcertSeat seat = concertSeatPort.get(input.concertSeatId());
        if (seat.isClosed()) {
            log.info("예약된 좌석에 예약 시도: uuid = {}, concertSeatId = {}", token.getKeyUuid(), seat.getId());
            throw new IllegalStateException("이미 예약된 좌석: " + seat.getId());
        }

        Payment payment = paymentPort.save(
                Payment.builder()
                        .userId(token.getUserId())
                        .price(seat.getPrice())
                        .status(PaymentStatus.PENDING)
                        .dueAt(LocalDateTime.now().plusMinutes(5L))
                        .build()
        );

        Reservation reservation = reservationPort.save(
                Reservation.builder()
                        .userId(token.getUserId())
                        .concertSeatId(input.concertSeatId())
                        .status(ReservationStatus.PENDING)
                        .paymentId(payment.getId())
                        .build()
        );

        seat.close();
        seat = concertSeatPort.save(seat);

        return new Output(
                new ReservationResult(
                        reservation.getId(),
                        reservation.getUserId(),
                        input.concertId(),
                        input.concertScheduleId(),
                        reservation.getConcertSeatId(),
                        reservation.getStatus(),
                        seat.getSeatNumber(),
                        payment.getPaymentKey()
                )
        );
    }
}
