package io.hhplus.concert.apps.integration.payment.usecase;

import io.hhplus.concert.app.payment.domain.Payment;
import io.hhplus.concert.app.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.app.payment.port.PaymentPort;
import io.hhplus.concert.app.payment.usecase.CompleteChargeUserPointUseCase;
import io.hhplus.concert.app.user.domain.PointTransaction;
import io.hhplus.concert.app.user.domain.UserPoint;
import io.hhplus.concert.app.user.domain.enm.PointTransactionStatus;
import io.hhplus.concert.app.user.domain.enm.PointTransactionType;
import io.hhplus.concert.app.user.port.PointTransactionPort;
import io.hhplus.concert.app.user.port.UserPointPort;
import io.hhplus.concert.config.IntegrationTestService;
import org.junit.jupiter.api.AfterEach;
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
    private IntegrationTestService integrationTestService;

    @Autowired
    private CompleteChargeUserPointUseCase completeChargeUserPointUseCase;

    @Autowired
    private PaymentPort paymentPort;

    @Autowired
    private UserPointPort userPointPort;

    @Autowired
    private PointTransactionPort pointTransactionPort;

    private String paymentKey;

    @BeforeEach
    public void setUp() {
        UserPoint userPoint = UserPoint.builder()
                .userId(1L)
                .remains(0)
                .build();
        userPointPort.save(userPoint);

        Payment payment = paymentPort.save(
                Payment.builder()
                        .price(BigDecimal.valueOf(50000))
                        .userId(1L)
                        .dueAt(LocalDateTime.now().plusMinutes(5))
                        .status(PaymentStatus.PENDING)
                        .build()
        );
        paymentKey = payment.getPaymentKey();

        PointTransaction pointTransaction = pointTransactionPort.save(
                PointTransaction.builder()
                        .userPointId(userPoint.getId())
                        .amount(50000)
                        .type(PointTransactionType.CHARGE)
                        .status(PointTransactionStatus.PENDING)
                        .paymentId(payment.getId())
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build()
        );
    }

    @AfterEach
    public void tearDown() {
        integrationTestService.tearDown();
    }

    @Test
    @DisplayName("결제가 정상 완료되면 포인트가 충전된다")
    public void completeChargeUserPoint() {
        CompleteChargeUserPointUseCase.Output output = completeChargeUserPointUseCase.execute(
                new CompleteChargeUserPointUseCase.Input(

                        "99543f87-9280-45f8-9a56-84a3a3d1312b",
                        1L,
                        paymentKey
                )
        );

        UserPoint userPoint = userPointPort.getByUserId(1L);
        assertEquals(50000, userPoint.getRemains());
    }
}
