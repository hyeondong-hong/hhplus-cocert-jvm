package io.hhplus.concert.integration.payment.usecase;

import io.hhplus.concert.app.payment.domain.Payment;
import io.hhplus.concert.app.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.app.payment.port.PaymentPort;
import io.hhplus.concert.app.payment.port.PaymentTransactionPort;
import io.hhplus.concert.app.payment.usecase.CompleteChargeUserPointUseCase;
import io.hhplus.concert.app.user.domain.PointTransaction;
import io.hhplus.concert.app.user.domain.UserPoint;
import io.hhplus.concert.app.user.domain.enm.PointTransactionStatus;
import io.hhplus.concert.app.user.domain.enm.PointTransactionType;
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
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class CompleteChargeUserPointUseCaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
