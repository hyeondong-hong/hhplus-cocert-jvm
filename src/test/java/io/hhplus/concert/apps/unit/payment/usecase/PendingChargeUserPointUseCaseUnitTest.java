package io.hhplus.concert.apps.unit.payment.usecase;

import io.hhplus.concert.app.payment.domain.Payment;
import io.hhplus.concert.app.payment.port.PaymentPort;
import io.hhplus.concert.app.payment.usecase.PendingChargeUserPointUseCase;
import io.hhplus.concert.app.user.domain.PointTransaction;
import io.hhplus.concert.app.user.domain.UserPoint;
import io.hhplus.concert.app.user.port.PointTransactionPort;
import io.hhplus.concert.app.user.port.UserPointPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PendingChargeUserPointUseCaseUnitTest {

    @Mock
    private PaymentPort paymentPort;

    @Mock
    private UserPointPort userPointPort;

    @Mock
    private PointTransactionPort pointTransactionPort;

    @InjectMocks
    private PendingChargeUserPointUseCase pendingChargeUserPointUseCase;

    private Long userId;
    private UserPoint userPoint;

    private Payment createdPayment;
    private PointTransaction createdPointTransaction;

    @BeforeEach
    public void setUp() {
        userId = 64L;
        userPoint = UserPoint.builder()
                .id(65L)
                .userId(userId)
                .remains(5000)
                .build();
    }

    @AfterEach
    public void tearDown() {
        createdPayment = null;
        createdPointTransaction = null;
    }

    @Test
    @DisplayName("포인트 충전을 요청하면 포인트 거래 정보가 생성된다")
    public void createPointTransaction() {
        when(userPointPort.getByUserId(eq(userPoint.getUserId()))).thenReturn(userPoint);
        when(paymentPort.save(any(Payment.class))).then(r -> {
            Payment origin = r.getArgument(0);
            return createdPayment = Payment.builder()
                    .id(1280L)
                    .userId(origin.getUserId())
                    .paymentKey(origin.getPaymentKey())
                    .price(origin.getPrice())
                    .status(origin.getStatus())
                    .dueAt(origin.getDueAt())
                    .paidAt(origin.getPaidAt())
                    .build();
        });
        when(pointTransactionPort.save(any(PointTransaction.class))).then(r -> {
            PointTransaction origin = r.getArgument(0);
            return createdPointTransaction = PointTransaction.builder()
                    .id(1440L)
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

        pendingChargeUserPointUseCase.execute(
                new PendingChargeUserPointUseCase.Input(
                        userId,
                        BigDecimal.valueOf(5000)
                )
        );

        assertNotNull(createdPayment);
        assertNotNull(createdPointTransaction);
    }
}
