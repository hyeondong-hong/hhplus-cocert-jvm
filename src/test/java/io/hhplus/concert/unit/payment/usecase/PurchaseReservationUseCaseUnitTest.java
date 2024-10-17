package io.hhplus.concert.unit.payment.usecase;

import io.hhplus.concert.concert.domain.Reservation;
import io.hhplus.concert.concert.domain.enm.ReservationStatus;
import io.hhplus.concert.concert.port.ConcertPort;
import io.hhplus.concert.concert.port.ConcertSchedulePort;
import io.hhplus.concert.concert.port.ConcertSeatPort;
import io.hhplus.concert.concert.port.ReservationPort;
import io.hhplus.concert.payment.domain.Payment;
import io.hhplus.concert.payment.domain.PaymentTransaction;
import io.hhplus.concert.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.payment.port.PaymentPort;
import io.hhplus.concert.payment.port.PaymentTransactionPort;
import io.hhplus.concert.payment.usecase.PurchaseReservationUseCase;
import io.hhplus.concert.user.domain.PointTransaction;
import io.hhplus.concert.user.domain.UserPoint;
import io.hhplus.concert.user.port.PointTransactionPort;
import io.hhplus.concert.user.port.UserPointPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PurchaseReservationUseCaseUnitTest {

    @Mock
    private PaymentPort paymentPort;

    @Mock
    private PaymentTransactionPort paymentTransactionPort;

    @Mock
    private UserPointPort userPointPort;

    @Mock
    private ConcertPort concertPort;

    @Mock
    private ConcertSchedulePort concertSchedulePort;

    @Mock
    private ConcertSeatPort concertSeatPort;

    @Mock
    private ReservationPort reservationPort;

    @Mock
    private PointTransactionPort pointTransactionPort;

    @InjectMocks
    private PurchaseReservationUseCase purchaseReservationUseCase;

    private String paymentKey;
    private PurchaseReservationUseCase.Input input;
    private Reservation reservation;
    private Payment payment;
    private UserPoint userPoint;

    @BeforeEach
    public void setUp() {
        paymentKey = UUID.randomUUID().toString();
        input = new PurchaseReservationUseCase.Input(
                1L,
                3L,
                127L,
                512L,
                paymentKey
        );

        reservation = new Reservation();
        reservation.setId(512L);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setUserId(64L);
        reservation.setConcertSeatId(127L);
        reservation.setPaymentId(1024L);

        payment = new Payment();
        payment.setId(1024L);
        payment.setPaymentKey(paymentKey);
        payment.setUserId(64L);
        payment.setDueAt(LocalDateTime.now().plusDays(5));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPrice(BigDecimal.valueOf(12000));

        userPoint = new UserPoint();
        userPoint.setId(65L);
        userPoint.setUserId(64L);
        userPoint.setRemains(40000);

        when(concertPort.existsById(any(Long.class))).thenReturn(true);
        lenient().when(concertSchedulePort.existsById(any(Long.class))).thenReturn(true);
        lenient().when(concertSeatPort.existsById(any(Long.class))).thenReturn(true);
        lenient().when(reservationPort.getWithLock(eq(reservation.getId()))).thenReturn(reservation);
        lenient().when(paymentPort.getByPaymentKeyWithLock(eq(paymentKey))).thenReturn(payment);
        lenient().when(userPointPort.getByUserIdWithLock(eq(userPoint.getUserId()))).thenReturn(userPoint);
    }

    @Test
    @DisplayName("콘서트가 없으면 예외가 발생한다")
    public void noConcerts() {
        when(concertPort.existsById(any(Long.class))).thenReturn(false);

        NoSuchElementException e = assertThrows(
                NoSuchElementException.class, () -> purchaseReservationUseCase.execute(input));

        assertEquals("Concert not found: " + input.concertId(), e.getMessage());
    }

    @Test
    @DisplayName("스케줄이 없으면 예외가 발생한다")
    public void noSchedules() {
        when(concertSchedulePort.existsById(any(Long.class))).thenReturn(false);

        NoSuchElementException e = assertThrows(
                NoSuchElementException.class, () -> purchaseReservationUseCase.execute(input));

        assertEquals("Concert Schedule not found: " + input.concertScheduleId(), e.getMessage());
    }

    @Test
    @DisplayName("좌석이 없으면 예외가 발생한다")
    public void noSeats() {
        when(concertSeatPort.existsById(any(Long.class))).thenReturn(false);

        NoSuchElementException e = assertThrows(
                NoSuchElementException.class, () -> purchaseReservationUseCase.execute(input));

        assertEquals("Concert Seat not found: " + input.concertSeatId(), e.getMessage());
    }

    @Test
    @DisplayName("예약 정보와 맞지 않은 결제 키를 사용하면 예외가 발생한다")
    public void noMatchPaymentKey() {
        String differentPaymentKey = UUID.randomUUID().toString();
        when(paymentPort.getByPaymentKeyWithLock(eq(differentPaymentKey))).then(r -> {
            Payment p = new Payment();
            p.setId(1280L);
            p.setPaymentKey(differentPaymentKey);
            return p;
        });

        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseReservationUseCase.execute(
                        new PurchaseReservationUseCase.Input(
                                input.concertId(),
                                input.concertScheduleId(),
                                input.concertSeatId(),
                                input.reservationId(),
                                differentPaymentKey
                        )
                )
        );

        assertEquals("유효하지 않은 결제 키", e.getMessage());
    }

    @Test
    @DisplayName("이미 완료된 예약에 대해 재요청을 하면 예외가 발생한다")
    public void completedReservation() {
        reservation.setStatus(ReservationStatus.COMPLETE);

        IllegalStateException e = assertThrows(
                IllegalStateException.class,
                () -> purchaseReservationUseCase.execute(input)
        );

        assertEquals("이미 예약이 완료됨", e.getMessage());
    }

    @Test
    @DisplayName("이미 취소된 예약에 대해 요청을 하면 예외가 발생한다")
    public void cancelledReservation() {
        reservation.setStatus(ReservationStatus.CANCELLED);

        IllegalStateException e = assertThrows(
                IllegalStateException.class,
                () -> purchaseReservationUseCase.execute(input)
        );

        assertEquals("취소된 예약", e.getMessage());
    }

    @Test
    @DisplayName("포인트가 결제해야할 금액보다 적으면 결제가 이뤄지지 않아야 한다")
    public void pointNotEnough() {
        userPoint.setRemains(payment.getPrice().intValue() - 1000);
        lenient().when(pointTransactionPort.save(any(PointTransaction.class))).thenThrow(AssertionError.class);

        PurchaseReservationUseCase.Output output = purchaseReservationUseCase.execute(input);

        assertEquals(ReservationStatus.PENDING, output.reservationStatus());
        assertEquals(PaymentStatus.PENDING, output.purchaseResult().paymentStatus());
    }

    @Test
    @DisplayName("절차가 완료되면 예약은 결제와 함께 완료 상태로 변경된다")
    public void complete() {
        when(userPointPort.save(any(UserPoint.class))).then(r -> r.getArgument(0));
        when(pointTransactionPort.save(any(PointTransaction.class))).then(r -> {
            PointTransaction result = r.getArgument(0);
            result.setId(2560L);
            return result;
        });
        when(paymentTransactionPort.save(any(PaymentTransaction.class))).then(r -> {
            PaymentTransaction result = r.getArgument(0);
            result.setId(2160L);
            return result;
        });
        when(reservationPort.save(any(Reservation.class))).then(r -> r.getArgument(0));
        when(paymentPort.save(any(Payment.class))).then(r -> r.getArgument(0));

        PurchaseReservationUseCase.Output output = purchaseReservationUseCase.execute(input);

        assertEquals(ReservationStatus.COMPLETE, output.reservationStatus());
        assertEquals(PaymentStatus.PAID, output.purchaseResult().paymentStatus());
        assertNotNull(output.purchaseResult().paidAt());
    }
}
