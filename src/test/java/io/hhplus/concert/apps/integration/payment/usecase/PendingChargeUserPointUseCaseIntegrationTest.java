package io.hhplus.concert.apps.integration.payment.usecase;

import io.hhplus.concert.app.payment.domain.Payment;
import io.hhplus.concert.app.payment.port.PaymentPort;
import io.hhplus.concert.app.payment.usecase.PendingChargeUserPointUseCase;
import io.hhplus.concert.app.user.domain.UserPoint;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class PendingChargeUserPointUseCaseIntegrationTest {

    @Autowired
    private IntegrationTestService integrationTestService;

    @Autowired
    private PendingChargeUserPointUseCase pendingChargeUserPointUseCase;

    @Autowired
    private PaymentPort paymentPort;

    @Autowired
    private UserPointPort userPointPort;

    @BeforeEach
    public void setUp() {
        userPointPort.save(
                UserPoint.builder()
                        .userId(1L)
                        .build()
        );
    }

    @AfterEach
    public void tearDown() {
        integrationTestService.tearDown();
    }

    @Test
    @DisplayName("특정 금액 충전을 요청하면 해당하는 결제 정보가 추가된다")
    public void pendingChargeUserPoint() {
        PendingChargeUserPointUseCase.Output output = pendingChargeUserPointUseCase.execute(
                new PendingChargeUserPointUseCase.Input(
                        1L,
                        BigDecimal.valueOf(50000)
                )
        );

        Payment payment = paymentPort.getByPaymentKey(output.pendingPointChargeResult().paymentKey());
        assertEquals(50000, payment.getPrice().intValue());
    }
}
