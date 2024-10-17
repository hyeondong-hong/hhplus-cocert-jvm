package io.hhplus.concert.unit.payment.usecase;

import io.hhplus.concert.payment.domain.Payment;
import io.hhplus.concert.payment.port.PaymentPort;
import io.hhplus.concert.payment.usecase.PendingChargeUserPointUseCase;
import io.hhplus.concert.user.domain.PointTransaction;
import io.hhplus.concert.user.domain.UserPoint;
import io.hhplus.concert.user.port.PointTransactionPort;
import io.hhplus.concert.user.port.UserPointPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        userPoint = new UserPoint();
        userPoint.setId(65L);
        userPoint.setUserId(userId);
        userPoint.setRemains(5000);
    }

    @AfterEach
    public void tearDown() {
        createdPayment = null;
        createdPointTransaction = null;
    }

    @Test
    @DisplayName("포인트 충전을 요청하면 포인트 거래 정보가 생성된다")
    public void createPointTransaction() {
        when(userPointPort.getByUserIdWithLock(eq(userPoint.getUserId()))).thenReturn(userPoint);
        when(paymentPort.save(any(Payment.class))).then(r -> {
            createdPayment = r.getArgument(0);
            createdPayment.setId(1280L);
            return createdPayment;
        });
        when(pointTransactionPort.save(any(PointTransaction.class))).then(r -> {
            createdPointTransaction = r.getArgument(0);
            createdPointTransaction.setId(1440L);
            return createdPointTransaction;
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
