package io.hhplus.concert.concert.usecase;

import io.hhplus.concert.concert.domain.ConcertSeat;
import io.hhplus.concert.concert.domain.Reservation;
import io.hhplus.concert.concert.domain.enm.ReservationStatus;
import io.hhplus.concert.concert.port.ConcertPort;
import io.hhplus.concert.concert.port.ConcertSchedulePort;
import io.hhplus.concert.concert.port.ConcertSeatPort;
import io.hhplus.concert.concert.port.ReservationPort;
import io.hhplus.concert.concert.usecase.dto.ReservationResult;
import io.hhplus.concert.payment.domain.Payment;
import io.hhplus.concert.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.payment.port.PaymentPort;
import io.hhplus.concert.user.domain.Token;
import io.hhplus.concert.user.port.TokenPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

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

        if (!concertPort.existsById(input.concertId())) {
            throw new NoSuchElementException("Concert not found: " + input.concertId());
        }

        if (!concertSchedulePort.existsById(input.concertScheduleId())) {
            throw new NoSuchElementException("Concert Schedule not found: " + input.concertScheduleId());
        }

        ConcertSeat seat = concertSeatPort.getWithLock(input.concertSeatId());

        if (!seat.getIsActive()) {
            throw new IllegalStateException("이미 예약된 좌석: " + seat.getId());
        }

        Payment payment = new Payment();
        payment.setUserId(token.getUserId());
        payment.setPrice(seat.getPrice());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setDueAt(LocalDateTime.now().plusMinutes(5L));
        payment = paymentPort.save(payment);

        Reservation reservation = new Reservation();
        reservation.setUserId(token.getUserId());
        reservation.setConcertSeatId(input.concertSeatId());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setPaymentId(payment.getId());
        reservation = reservationPort.save(reservation);

        seat.setIsActive(false);
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
