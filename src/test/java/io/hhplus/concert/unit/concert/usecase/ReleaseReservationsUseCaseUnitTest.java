package io.hhplus.concert.unit.concert.usecase;

import io.hhplus.concert.app.concert.domain.Reservation;
import io.hhplus.concert.app.concert.domain.enm.ReservationStatus;
import io.hhplus.concert.app.concert.port.ConcertSeatPort;
import io.hhplus.concert.app.concert.port.ReservationPort;
import io.hhplus.concert.app.concert.usecase.ReleaseReservationsUseCase;
import io.hhplus.concert.app.payment.domain.Payment;
import io.hhplus.concert.app.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.app.payment.port.PaymentPort;
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

    @Mock
    private ConcertSeatPort concertSeatPort;

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
                selectedReservations.add(
                        Reservation.builder()
                                .id(i + 1L)
                                .userId(64L + (i * 10))
                                .concertSeatId(70L + i)
                                .paymentId(1024L + i)
                                .status(ReservationStatus.PENDING)
                                .build()
                );
            }
            return selectedReservations;
        });
        when(paymentPort.getExpiredAllByIdsWithLock(any(List.class))).then(r -> {
            selectedPayments = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                selectedPayments.add(
                        Payment.builder()
                                .id(i + 1024L)
                                .userId(64L + (i * 10))
                                .dueAt(LocalDateTime.now().minusMinutes(5))
                                .status(PaymentStatus.PENDING)
                                .price(BigDecimal.valueOf(12000))
                                .paymentKey(UUID.randomUUID().toString())
                                .build()
                );
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
