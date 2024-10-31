package io.hhplus.concert.app.concert.usecase;

import io.hhplus.concert.app.concert.domain.ConcertSeat;
import io.hhplus.concert.app.concert.domain.Reservation;
import io.hhplus.concert.app.concert.domain.enm.ReservationStatus;
import io.hhplus.concert.app.concert.port.ConcertSeatPort;
import io.hhplus.concert.app.concert.port.ReservationPort;
import io.hhplus.concert.app.payment.domain.Payment;
import io.hhplus.concert.app.payment.port.PaymentPort;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class ReleaseReservationsUseCase {

    private final ConcertSeatPort concertSeatPort;
    private final ReservationPort reservationPort;
    private final PaymentPort paymentPort;

    public record Input(
    ) { }

    public record Output(
    ) { }

    @Transactional
    public Output execute(Input input) {
        // reservation -> payment 순서대로 비관락
        // 다른 트랜잭션 블록에서 payment -> reservation 순서로 처리 시 데드락에 주의
        List<Reservation> originReservations = reservationPort.getAllByStatusesWithLock(
                List.of(ReservationStatus.PENDING)
        );

        if (originReservations.isEmpty()) {
            return new Output();
        }

        List<Payment> payments = paymentPort.getExpiredAllByIdsWithLock(
                originReservations.stream().map(Reservation::getPaymentId).toList()
        );

        if (payments.isEmpty()) {
            return new Output();
        }

        payments.forEach(Payment::cancel);

        Set<Long> paymentIdSet = payments.stream().map(Payment::getId).collect(Collectors.toUnmodifiableSet());
        List<Reservation> reservations = originReservations.stream().filter(
                reservation -> paymentIdSet.contains(reservation.getPaymentId())).toList();

        reservations.forEach(Reservation::setCancelled);

        List<ConcertSeat> seats = concertSeatPort.getAllByIdsWithLock(
                reservations.stream().map(Reservation::getConcertSeatId).toList());
        seats.forEach(ConcertSeat::open);

        log.info("Schedule: Released Payments: {}", payments.stream().map(Payment::getId).toList());
        log.info("Schedule: Released Reservations: {}", reservations.stream().map(Reservation::getId).toList());
        log.info("Schedule: Released Concert Seats: {}", seats.stream().map(ConcertSeat::getId).toList());

        concertSeatPort.saveAll(seats);
        paymentPort.saveAll(payments);
        reservationPort.saveAll(reservations);

        return new Output();
    }
}
