package io.hhplus.concert.integration.payment.usecase;

import io.hhplus.concert.app.payment.domain.Payment;
import io.hhplus.concert.app.payment.port.PaymentPort;
import io.hhplus.concert.app.payment.usecase.PendingChargeUserPointUseCase;
import io.hhplus.concert.app.user.domain.UserPoint;
import io.hhplus.concert.app.user.port.PointTransactionPort;
import io.hhplus.concert.app.user.port.UserPointPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class PendingChargeUserPointUseCaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
        jdbcTemplate.execute("truncate table hhplus_concert_test.concert;");
        jdbcTemplate.execute("truncate table hhplus_concert_test.concert_schedule;");
        jdbcTemplate.execute("truncate table hhplus_concert_test.concert_seat;");
        jdbcTemplate.execute("truncate table hhplus_concert_test.payment;");
        jdbcTemplate.execute("truncate table hhplus_concert_test.payment_transaction;");
        jdbcTemplate.execute("truncate table hhplus_concert_test.point_transaction;");
        jdbcTemplate.execute("truncate table hhplus_concert_test.reservation;");
        jdbcTemplate.execute("truncate table hhplus_concert_test.user;");
        jdbcTemplate.execute("truncate table hhplus_concert_test.user_point;");
        jdbcTemplate.execute("truncate table hhplus_concert_test.token;");
        jdbcTemplate.execute("truncate table hhplus_concert_test.service_entry;");
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
