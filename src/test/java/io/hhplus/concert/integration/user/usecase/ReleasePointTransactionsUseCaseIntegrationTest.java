package io.hhplus.concert.integration.user.usecase;

import io.hhplus.concert.app.payment.domain.Payment;
import io.hhplus.concert.app.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.app.payment.port.PaymentPort;
import io.hhplus.concert.app.user.domain.PointTransaction;
import io.hhplus.concert.app.user.domain.enm.PointTransactionStatus;
import io.hhplus.concert.app.user.domain.enm.PointTransactionType;
import io.hhplus.concert.app.user.port.PointTransactionPort;
import io.hhplus.concert.app.user.usecase.ReleasePointTransactionsUseCase;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class ReleasePointTransactionsUseCaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReleasePointTransactionsUseCase releasePointTransactionsUseCase;

    @Autowired
    private PointTransactionPort pointTransactionPort;

    @Autowired
    private PaymentPort paymentPort;

    @BeforeEach
    public void setUp() {
        List<PointTransaction> pointTransactions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            pointTransactions.add(
                    PointTransaction.builder()
                            .userPointId(10 + 1L)
                            .type(PointTransactionType.CHARGE)
                            .status(PointTransactionStatus.PENDING)
                            .remains(0)
                            .paymentId(i + 1L)
                            .amount(3000)
                            .createdAt(LocalDateTime.now())
                            .modifiedAt(LocalDateTime.now())
                            .build()
            );
        }
        pointTransactionPort.saveAll(pointTransactions);

        List<Payment> payments = new ArrayList<>();
        for (int i = 0; i < pointTransactions.size() - 5; i++) {
            payments.add(
                    Payment.builder()
                            .price(BigDecimal.valueOf(3000))
                            .dueAt(LocalDateTime.now().minusDays(1))
                            .userId(1L)
                            .status(PaymentStatus.PENDING)
                            .paidAt(null)
                            .build()
            );
        }
        paymentPort.saveAll(payments);
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
    @DisplayName("만료된 결제 정보가 존재하면 해당 결제 정보에 연결된 포인트 거래내역만 취소 처리 한다.")
    public void cancelExpired() {
        releasePointTransactionsUseCase.execute(new ReleasePointTransactionsUseCase.Input());
        Payment payment = paymentPort.get(1L);
        assertEquals(payment.getStatus(), PaymentStatus.CANCELLED);
    }
}
