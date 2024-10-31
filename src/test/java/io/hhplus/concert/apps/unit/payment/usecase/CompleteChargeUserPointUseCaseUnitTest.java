package io.hhplus.concert.apps.unit.payment.usecase;

import io.hhplus.concert.app.payment.domain.Payment;
import io.hhplus.concert.app.payment.domain.PaymentTransaction;
import io.hhplus.concert.app.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.app.payment.domain.enm.PgResultType;
import io.hhplus.concert.app.payment.port.HangHaePgPort;
import io.hhplus.concert.app.payment.port.PaymentPort;
import io.hhplus.concert.app.payment.port.PaymentTransactionPort;
import io.hhplus.concert.app.payment.usecase.CompleteChargeUserPointUseCase;
import io.hhplus.concert.app.payment.usecase.dto.PointChangeResult;
import io.hhplus.concert.app.user.domain.PointTransaction;
import io.hhplus.concert.app.user.domain.UserPoint;
import io.hhplus.concert.app.user.domain.enm.PointTransactionStatus;
import io.hhplus.concert.app.user.domain.enm.PointTransactionType;
import io.hhplus.concert.app.user.port.PointTransactionPort;
import io.hhplus.concert.app.user.port.UserPointPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(MockitoExtension.class)
public class CompleteChargeUserPointUseCaseUnitTest {

    @Mock
    private HangHaePgPort hangHaePgPort;

    @Mock
    private PaymentPort paymentPort;

    @Mock
    private PaymentTransactionPort paymentTransactionPort;

    @Mock
    private UserPointPort userPointPort;

    @Mock
    private PointTransactionPort pointTransactionPort;

    @InjectMocks
    private CompleteChargeUserPointUseCase completeChargeUserPointUseCase;

    private Long userId;
    private String paymentKey;

    private Payment payment;

    private Payment expiredPayment;

    private UserPoint userPoint;

    private PointTransaction pointTransaction;

    @BeforeEach
    public void setUp() {
        userId = 64L;
        paymentKey = UUID.randomUUID().toString();

        payment = Payment.builder()
                .id(1280L)
                .userId(userId)
                .paymentKey(paymentKey)
                .status(PaymentStatus.PENDING)
                .dueAt(LocalDateTime.now().plusMinutes(5))
                .price(BigDecimal.valueOf(5000))
                .build();

        userPoint = UserPoint.builder()
                .id(65L)
                .userId(payment.getUserId())
                .remains(5000)
                .build();

        pointTransaction = PointTransaction.builder()
                .id(2560L)
                .userPointId(userPoint.getId())
                .remains(10000)
                .amount(5000)
                .status(PointTransactionStatus.PENDING)
                .type(PointTransactionType.CHARGE)
                .paymentId(payment.getId())
                .build();

        lenient().when(paymentPort.getByPaymentKey(eq(paymentKey))).thenReturn(payment);
        lenient().when(hangHaePgPort.purchase(eq(userId), eq(paymentKey), any(BigDecimal.class))).thenReturn(PgResultType.OK);
        lenient().when(pointTransactionPort.getByPaymentId(eq(payment.getId()))).thenReturn(pointTransaction);
        lenient().when(pointTransactionPort.save(eq(pointTransaction))).thenReturn(pointTransaction);
        lenient().when(userPointPort.getByUserId(eq(userId))).thenReturn(userPoint);
        lenient().when(userPointPort.save(eq(userPoint))).thenReturn(userPoint);
        lenient().when(paymentPort.save(eq(payment))).thenReturn(payment);
        lenient().when(paymentTransactionPort.save(any(PaymentTransaction.class))).then(r -> {
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
    }

    @Test
    @DisplayName("이미 완료된 결제에 대해서 예외가 발생한다")
    public void completedPayment() {
        payment.pay();

        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> completeChargeUserPointUseCase.execute(
                        new CompleteChargeUserPointUseCase.Input(
                                userId,
                                paymentKey
                        )
                )
        );

        assertEquals("이미 완료된 결제", e.getMessage());
    }

    @Test
    @DisplayName("취소된 결제에 대해서 예외가 발생한다")
    public void cancelledPayment() {
        payment.cancel();

        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> completeChargeUserPointUseCase.execute(
                        new CompleteChargeUserPointUseCase.Input(
                                userId,
                                paymentKey
                        )
                )
        );

        assertEquals("취소된 결제", e.getMessage());
    }

    @Test
    @DisplayName("결제 기한이 만료된 예약에 대해 예외가 발생한다")
    public void expiredPayment() {
        Payment expiredPayment = Payment.builder()
                .id(payment.getId())
                .userId(payment.getUserId())
                .paymentKey(payment.getPaymentKey())
                .status(payment.getStatus())
                .dueAt(LocalDateTime.now().minusMinutes(5))  // expired
                .price(payment.getPrice())
                .build();
        when(paymentPort.getByPaymentKey(eq(paymentKey))).thenReturn(expiredPayment);

        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> completeChargeUserPointUseCase.execute(
                        new CompleteChargeUserPointUseCase.Input(
                                userId,
                                paymentKey
                        )
                )
        );

        assertEquals("결제 기한 만료", e.getMessage());
    }

    @Test
    @DisplayName("PG사 모듈(임시) 에서 결제가 거부된다")
    public void rejectedPayment() {
        when(hangHaePgPort.purchase(eq(userId), eq(paymentKey), any(BigDecimal.class))).thenReturn(PgResultType.REJECTED_PAYMENT);

        AccessDeniedException e = assertThrows(
                AccessDeniedException.class,
                () -> completeChargeUserPointUseCase.execute(
                        new CompleteChargeUserPointUseCase.Input(
                                userId,
                                paymentKey
                        )
                )
        );

        assertEquals("승인 거부", e.getMessage());
    }

    @Test
    @DisplayName("결제가 완료되고 포인트가 충전된다")
    public void completePayment() {
        CompleteChargeUserPointUseCase.Output output = completeChargeUserPointUseCase.execute(
                new CompleteChargeUserPointUseCase.Input(
                        userId,
                        paymentKey
                )
        );

        PointChangeResult pointChangeResult = output.pointChangeResult();
        assertEquals(10000, pointChangeResult.remains());
    }
}
