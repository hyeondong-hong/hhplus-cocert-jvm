package io.hhplus.concert.integration.payment.usecase;

import io.hhplus.concert.payment.domain.Payment;
import io.hhplus.concert.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.payment.port.PaymentPort;
import io.hhplus.concert.payment.port.PaymentTransactionPort;
import io.hhplus.concert.payment.usecase.CompleteChargeUserPointUseCase;
import io.hhplus.concert.user.domain.PointTransaction;
import io.hhplus.concert.user.domain.UserPoint;
import io.hhplus.concert.user.domain.enm.PointTransactionStatus;
import io.hhplus.concert.user.domain.enm.PointTransactionType;
import io.hhplus.concert.user.port.PointTransactionPort;
import io.hhplus.concert.user.port.UserPointPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class CompleteChargeUserPointUseCaseIntegrationTest {

    @Autowired
    private CompleteChargeUserPointUseCase completeChargeUserPointUseCase;

    @Autowired
    private PaymentPort paymentPort;

    @Autowired
    private PaymentTransactionPort paymentTransactionPort;

    @Autowired
    private UserPointPort userPointPort;

    @Autowired
    private PointTransactionPort pointTransactionPort;

    private String paymentKey;

    @BeforeEach
    public void setUp() {
        UserPoint userPoint = new UserPoint();
        userPoint.setUserId(1L);
        userPoint.setRemains(0);
        userPointPort.save(userPoint);

        Payment payment = new Payment();
        payment.setPrice(BigDecimal.valueOf(50000));
        payment.setUserId(1L);
        payment.setDueAt(LocalDateTime.now().plusMinutes(5));
        payment.setStatus(PaymentStatus.PENDING);
        paymentPort.save(payment);
        paymentKey = payment.getPaymentKey();

        PointTransaction pointTransaction = new PointTransaction();
        pointTransaction.setUserPointId(userPoint.getId());
        pointTransaction.setAmount(50000);
        pointTransaction.setType(PointTransactionType.CHARGE);
        pointTransaction.setStatus(PointTransactionStatus.PENDING);
        pointTransaction.setPaymentId(payment.getId());
        pointTransaction.setCreatedAt(LocalDateTime.now());
        pointTransaction.setModifiedAt(LocalDateTime.now());
        pointTransactionPort.save(pointTransaction);
    }

    @Test
    @DisplayName("결제가 정상 완료되면 포인트가 충전된다")
    public void completeChargeUserPoint() {
        CompleteChargeUserPointUseCase.Output output = completeChargeUserPointUseCase.execute(
                new CompleteChargeUserPointUseCase.Input(
                        1L,
                        paymentKey
                )
        );

        UserPoint userPoint = userPointPort.getByUserId(1L);
        assertEquals(50000, userPoint.getRemains());
    }
}
