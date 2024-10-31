package io.hhplus.concert.apps.unit.payment.usecase;

import io.hhplus.concert.app.concert.domain.Reservation;
import io.hhplus.concert.app.concert.domain.enm.ReservationStatus;
import io.hhplus.concert.app.concert.port.ConcertPort;
import io.hhplus.concert.app.concert.port.ConcertSchedulePort;
import io.hhplus.concert.app.concert.port.ConcertSeatPort;
import io.hhplus.concert.app.concert.port.ReservationPort;
import io.hhplus.concert.app.payment.domain.Payment;
import io.hhplus.concert.app.payment.domain.PaymentTransaction;
import io.hhplus.concert.app.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.app.payment.port.PaymentPort;
import io.hhplus.concert.app.payment.port.PaymentTransactionPort;
import io.hhplus.concert.app.payment.usecase.PurchaseReservationUseCase;
import io.hhplus.concert.app.user.domain.PointTransaction;
import io.hhplus.concert.app.user.domain.UserPoint;
import io.hhplus.concert.app.user.port.PointTransactionPort;
import io.hhplus.concert.app.user.port.UserPointPort;
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
import static org.mockito.Mockito.*;

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

        reservation = Reservation.builder()
                .id(512L)
                .status(ReservationStatus.PENDING)
                .userId(64L)
                .concertSeatId(127L)
                .paymentId(1024L)
                .build();

        payment = Payment.builder()
                .id(1024L)
                .paymentKey(paymentKey)
                .userId(64L)
                .dueAt(LocalDateTime.now().plusDays(5))
                .status(PaymentStatus.PENDING)
                .price(BigDecimal.valueOf(12000))
                .build();

        userPoint = UserPoint.builder()
                .id(65L)
                .userId(64L)
                .remains(40000)
                .build();

        lenient().when(concertPort.existsById(any(Long.class))).thenReturn(true);
        lenient().when(concertSchedulePort.existsById(any(Long.class))).thenReturn(true);
        lenient().when(concertSeatPort.existsById(any(Long.class))).thenReturn(true);
        lenient().when(reservationPort.get(eq(reservation.getId()))).thenReturn(reservation);
        lenient().when(paymentPort.getByPaymentKey(eq(paymentKey))).thenReturn(payment);
        lenient().when(userPointPort.getByUserId(eq(userPoint.getUserId()))).thenReturn(userPoint);
    }

    @Test
    @DisplayName("콘서트가 없으면 예외가 발생한다")
    public void noConcerts() {
        doThrow(new NoSuchElementException("Concert not found: " + input.concertId())).when(concertPort).existsOrThrow(any(Long.class));

        NoSuchElementException e = assertThrows(
                NoSuchElementException.class, () -> purchaseReservationUseCase.execute(input));

        assertEquals("Concert not found: " + input.concertId(), e.getMessage());
    }

    @Test
    @DisplayName("스케줄이 없으면 예외가 발생한다")
    public void noSchedules() {
        doThrow(new NoSuchElementException("Concert Schedule not found: " + input.concertScheduleId())).when(concertSchedulePort).existsOrThrow(any(Long.class));

        NoSuchElementException e = assertThrows(
                NoSuchElementException.class, () -> purchaseReservationUseCase.execute(input));

        assertEquals("Concert Schedule not found: " + input.concertScheduleId(), e.getMessage());
    }

    @Test
    @DisplayName("좌석이 없으면 예외가 발생한다")
    public void noSeats() {
        doThrow(new NoSuchElementException("Concert Seat not found: " + input.concertSeatId())).when(concertSeatPort).existsOrThrow(any(Long.class));

        NoSuchElementException e = assertThrows(
                NoSuchElementException.class, () -> purchaseReservationUseCase.execute(input));

        assertEquals("Concert Seat not found: " + input.concertSeatId(), e.getMessage());
    }

    @Test
    @DisplayName("예약 정보와 맞지 않은 결제 키를 사용하면 예외가 발생한다")
    public void noMatchPaymentKey() {
        String differentPaymentKey = UUID.randomUUID().toString();
        when(paymentPort.getByPaymentKey(eq(differentPaymentKey))).then(r ->
                Payment.builder()
                        .id(1280L)
                        .paymentKey(differentPaymentKey).build()
        );

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
        reservation.setCompleted();

        IllegalStateException e = assertThrows(
                IllegalStateException.class,
                () -> purchaseReservationUseCase.execute(input)
        );

        assertEquals("이미 예약이 완료됨", e.getMessage());
    }

    @Test
    @DisplayName("이미 취소된 예약에 대해 요청을 하면 예외가 발생한다")
    public void cancelledReservation() {
        reservation.setCancelled();

        IllegalStateException e = assertThrows(
                IllegalStateException.class,
                () -> purchaseReservationUseCase.execute(input)
        );

        assertEquals("취소된 예약", e.getMessage());
    }

    @Test
    @DisplayName("포인트가 결제해야할 금액보다 적으면 결제가 이뤄지지 않아야 한다")
    public void pointNotEnough() {
        userPoint.deduct(30000);  // 40,000 - 30,000
        lenient().when(pointTransactionPort.save(any(PointTransaction.class))).thenThrow(AssertionError.class);

        IllegalStateException e = assertThrows(
                IllegalStateException.class,
                () -> purchaseReservationUseCase.execute(input)
        );

        assertEquals("잔여 포인트 부족: (remains = 10000 < amount = 12000)", e.getMessage());
    }

    @Test
    @DisplayName("절차가 완료되면 예약은 결제와 함께 완료 상태로 변경된다")
    public void complete() {
        when(userPointPort.save(any(UserPoint.class))).then(r -> r.getArgument(0));
        when(pointTransactionPort.save(any(PointTransaction.class))).then(r -> {
            PointTransaction origin = r.getArgument(0);
            return PointTransaction.builder()
                    .id(2560L)
                    .userPointId(origin.getUserPointId())
                    .remains(origin.getRemains())
                    .amount(origin.getAmount())
                    .type(origin.getType())
                    .status(origin.getStatus())
                    .paymentId(origin.getPaymentId())
                    .createdAt(origin.getCreatedAt())
                    .modifiedAt(origin.getModifiedAt())
                    .build();
        });
        when(paymentTransactionPort.save(any(PaymentTransaction.class))).then(r -> {
            PaymentTransaction origin = r.getArgument(0);
            return PaymentTransaction.builder()
                    .id(2160L)
                    .paymentId(origin.getPaymentId())
                    .method(origin.getMethod())
                    .status(origin.getStatus())
                    .amount(origin.getAmount())
                    .createdAt(origin.getCreatedAt())
                    .modifiedAt(origin.getModifiedAt())
                    .build();
        });
        when(reservationPort.save(any(Reservation.class))).then(r -> r.getArgument(0));
        when(paymentPort.save(any(Payment.class))).then(r -> r.getArgument(0));

        PurchaseReservationUseCase.Output output = purchaseReservationUseCase.execute(input);

        assertEquals(ReservationStatus.COMPLETE, output.reservationStatus());
        assertEquals(PaymentStatus.PAID, output.purchaseResult().paymentStatus());
        assertNotNull(output.purchaseResult().paidAt());
    }
}
