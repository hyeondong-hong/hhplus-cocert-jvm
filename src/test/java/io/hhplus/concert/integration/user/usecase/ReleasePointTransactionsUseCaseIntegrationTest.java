package io.hhplus.concert.integration.user.usecase;

import io.hhplus.concert.payment.domain.Payment;
import io.hhplus.concert.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.payment.port.PaymentPort;
import io.hhplus.concert.user.domain.PointTransaction;
import io.hhplus.concert.user.domain.enm.PointTransactionStatus;
import io.hhplus.concert.user.domain.enm.PointTransactionType;
import io.hhplus.concert.user.port.PointTransactionPort;
import io.hhplus.concert.user.usecase.ReleasePointTransactionsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
    private ReleasePointTransactionsUseCase releasePointTransactionsUseCase;

    @Autowired
    private PointTransactionPort pointTransactionPort;

    @Autowired
    private PaymentPort paymentPort;

    @BeforeEach
    public void setUp() {
        List<PointTransaction> pointTransactions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            PointTransaction pt = new PointTransaction();
            pt.setUserPointId(10 + 1L);
            pt.setType(PointTransactionType.CHARGE);
            pt.setStatus(PointTransactionStatus.PENDING);
            pt.setRemains(0);
            pt.setPaymentId(i + 1L);
            pt.setAmount(3000);
            pt.setCreatedAt(LocalDateTime.now());
            pt.setModifiedAt(LocalDateTime.now());
            pointTransactions.add(pt);
        }
        pointTransactionPort.saveAll(pointTransactions);

        List<Payment> payments = new ArrayList<>();
        for (int i = 0; i < pointTransactions.size() - 5; i++) {
            Payment p = new Payment();
            p.setPrice(BigDecimal.valueOf(3000));
            p.setDueAt(LocalDateTime.now().minusDays(1));
            p.setUserId(1L);
            p.setStatus(PaymentStatus.PENDING);
            p.setPaidAt(null);
            payments.add(p);
        }
        paymentPort.saveAll(payments);
    }

    @Test
    @DisplayName("만료된 결제 정보가 존재하면 해당 결제 정보에 연결된 포인트 거래내역만 취소 처리 한다.")
    public void cancelExpired() {
        releasePointTransactionsUseCase.execute(new ReleasePointTransactionsUseCase.Input());
        Payment payment = paymentPort.get(1L);
        assertEquals(payment.getStatus(), PaymentStatus.CANCELLED);
    }
}
