package io.hhplus.concert.unit.concert.usecase;

import io.hhplus.concert.concert.domain.Reservation;
import io.hhplus.concert.concert.domain.enm.ReservationStatus;
import io.hhplus.concert.concert.port.ReservationPort;
import io.hhplus.concert.concert.usecase.ReleaseReservationsUseCase;
import io.hhplus.concert.payment.domain.Payment;
import io.hhplus.concert.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.payment.port.PaymentPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReleaseReservationsUseCaseUnitTest {

    @Mock
    private ReservationPort reservationPort;

    @Mock
    private PaymentPort paymentPort;

    @InjectMocks
    private ReleaseReservationsUseCase releaseReservationsUseCase;

    private List<Reservation> selectedReservations = new ArrayList<>();
    private List<Payment> selectedPayments = new ArrayList<>();

    @AfterEach
    public void tearDown() {
        selectedReservations.clear();
        selectedPayments.clear();
    }

    @Test
    @DisplayName("임시 상태인 예약이 없으면 다음 스텝으로 넘어가지 않는다")
    public void noPendingReservations() {
        when(reservationPort.getAllByStatusesWithLock(any(List.class))).thenReturn(List.of());
        lenient().when(paymentPort.getExpiredAllByIdsWithLock(any(List.class))).thenThrow(AssertionError.class);

        assertDoesNotThrow(() -> releaseReservationsUseCase.execute(new ReleaseReservationsUseCase.Input()));
    }

    @Test
    @DisplayName("임시 상태의 예약이 있어도 만료된 결제 정보가 없으면 다음 스텝으로 넘어가지 않는다")
    public void noExpiredPayments() {
        when(reservationPort.getAllByStatusesWithLock(any(List.class))).thenReturn(List.of(new Reservation()));
        when(paymentPort.getExpiredAllByIdsWithLock(any(List.class))).thenReturn(List.of());
        lenient().when(paymentPort.saveAll(any(List.class))).thenThrow(AssertionError.class);

        assertDoesNotThrow(() -> releaseReservationsUseCase.execute(new ReleaseReservationsUseCase.Input()));
    }

    @Test
    @DisplayName("만료된 결제정보가 존재하면 취소 상태로 변경한다")
    public void releaseExpiredPayments() {
        when(reservationPort.getAllByStatusesWithLock(any(List.class))).then(r -> {
            selectedReservations = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                Reservation reservation = new Reservation();
                reservation.setId(i + 1L);
                reservation.setUserId(64L + (i * 10));
                reservation.setConcertSeatId(70L + i);
                reservation.setPaymentId(1024L + i);
                reservation.setStatus(ReservationStatus.PENDING);
                selectedReservations.add(reservation);
            }
            return selectedReservations;
        });
        when(paymentPort.getExpiredAllByIdsWithLock(any(List.class))).then(r -> {
            selectedPayments = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                Payment payment = new Payment();
                payment.setId(i + 1024L);
                payment.setUserId(64L + (i * 10));
                payment.setDueAt(LocalDateTime.now().minusMinutes(5));
                payment.setStatus(PaymentStatus.PENDING);
                payment.setPrice(BigDecimal.valueOf(12000));
                payment.setPaymentKey(UUID.randomUUID().toString());
                selectedPayments.add(payment);
            }
            return selectedPayments;
        });

        releaseReservationsUseCase.execute(
                new ReleaseReservationsUseCase.Input()
        );

        ReservationStatus[] reservationStatuses = {
                ReservationStatus.CANCELLED,
                ReservationStatus.CANCELLED,
                ReservationStatus.CANCELLED,
                ReservationStatus.PENDING,
                ReservationStatus.PENDING
        };

        PaymentStatus[] paymentStatuses = {
                PaymentStatus.CANCELLED,
                PaymentStatus.CANCELLED,
                PaymentStatus.CANCELLED
        };

        assertArrayEquals(reservationStatuses, selectedReservations.stream().map(Reservation::getStatus).toArray());
        assertArrayEquals(paymentStatuses, selectedPayments.stream().map(Payment::getStatus).toArray());
    }
}
