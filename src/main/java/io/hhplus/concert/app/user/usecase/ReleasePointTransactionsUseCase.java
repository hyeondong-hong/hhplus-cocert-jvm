package io.hhplus.concert.app.user.usecase;

import io.hhplus.concert.app.payment.domain.Payment;
import io.hhplus.concert.app.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.app.payment.port.PaymentPort;
import io.hhplus.concert.app.user.domain.PointTransaction;
import io.hhplus.concert.app.user.domain.enm.PointTransactionStatus;
import io.hhplus.concert.app.user.port.PointTransactionPort;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class ReleasePointTransactionsUseCase {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PointTransactionPort pointTransactionPort;
    private final PaymentPort paymentPort;

    public record Input(
    ) { }

    public record Output(
    ) { }

    @Transactional
    public Output execute(Input input) {
        // reservation -> payment 순서대로 비관락
        // 다른 트랜잭션 블록에서 payment -> reservation 순서로 처리 시 데드락에 주의
        List<PointTransaction> originPointTransactions = pointTransactionPort.getAllByStatusesWithLock(
                List.of(PointTransactionStatus.PENDING)
        );

        if (originPointTransactions.isEmpty()) {
            return new Output();
        }

        List<Payment> payments = paymentPort.getExpiredAllByIdsWithLock(
                originPointTransactions.stream().map(PointTransaction::getPaymentId).toList()
        );

        if (payments.isEmpty()) {
            return new Output();
        }

        payments.forEach(Payment::setCancelled);

        Set<Long> paymentIdSet = payments.stream().map(Payment::getId).collect(Collectors.toUnmodifiableSet());
        List<PointTransaction> pointTransactions = originPointTransactions.stream().filter(
                pointTransaction -> paymentIdSet.contains(pointTransaction.getPaymentId())).toList();

        pointTransactions.forEach(PointTransaction::setCancelled);

        logger.info("Schedule: Released Payments: {}", payments.stream().map(Payment::getId).toList());
        logger.info("Schedule: Released Point transactions: {}", pointTransactions.stream().map(PointTransaction::getId).toList());

        paymentPort.saveAll(payments);
        pointTransactionPort.saveAll(pointTransactions);

        return new Output();
    }
}
