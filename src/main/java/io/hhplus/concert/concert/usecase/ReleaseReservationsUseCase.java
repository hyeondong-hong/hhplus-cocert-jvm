package io.hhplus.concert.concert.usecase;

import io.hhplus.concert.concert.domain.ConcertSeat;
import io.hhplus.concert.concert.domain.Reservation;
import io.hhplus.concert.concert.domain.enm.ReservationStatus;
import io.hhplus.concert.concert.port.ConcertSeatPort;
import io.hhplus.concert.concert.port.ReservationPort;
import io.hhplus.concert.payment.domain.Payment;
import io.hhplus.concert.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.payment.port.PaymentPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        if (!originReservations.isEmpty()) {
            List<Payment> payments = paymentPort.getExpiredAllByIdsWithLock(
                    originReservations.stream().map(Reservation::getPaymentId).toList()
            );

            if (!payments.isEmpty()) {
                payments.forEach(payment -> payment.setStatus(PaymentStatus.CANCELLED));

                Set<Long> paymentIdSet = payments.stream().map(Payment::getId).collect(Collectors.toUnmodifiableSet());
                List<Reservation> reservations = originReservations.stream().filter(
                        reservation -> paymentIdSet.contains(reservation.getPaymentId())).toList();

                reservations.forEach(reservation -> reservation.setStatus(ReservationStatus.CANCELLED));

                List<ConcertSeat> seats = concertSeatPort.getAllByIdsWithLock(
                        reservations.stream().map(Reservation::getConcertSeatId).toList());
                seats.forEach(seat -> seat.setIsActive(true));

                concertSeatPort.saveAll(seats);
                paymentPort.saveAll(payments);
                reservationPort.saveAll(reservations);
            }
        }
        return new Output();
    }
}
