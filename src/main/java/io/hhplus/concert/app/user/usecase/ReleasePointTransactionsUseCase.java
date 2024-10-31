package io.hhplus.concert.app.user.usecase;

import io.hhplus.concert.app.payment.domain.Payment;
import io.hhplus.concert.app.payment.port.PaymentPort;
import io.hhplus.concert.app.user.domain.PointTransaction;
import io.hhplus.concert.app.user.domain.enm.PointTransactionStatus;
import io.hhplus.concert.app.user.port.PointTransactionPort;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class ReleasePointTransactionsUseCase {

    private final PointTransactionPort pointTransactionPort;
    private final PaymentPort paymentPort;

    public record Input(
    ) { }

    public record Output(
    ) { }

    @Transactional
    public Output execute(Input input) {
        List<PointTransaction> originPointTransactions = pointTransactionPort.getAllByStatuses(
                List.of(PointTransactionStatus.PENDING)
        );

        if (originPointTransactions.isEmpty()) {
            return new Output();
        }

        List<Payment> payments = paymentPort.getExpiredAllByIds(
                originPointTransactions.stream().map(PointTransaction::getPaymentId).toList()
        );

        if (payments.isEmpty()) {
            return new Output();
        }

        payments.forEach(Payment::cancel);

        Set<Long> paymentIdSet = payments.stream().map(Payment::getId).collect(Collectors.toUnmodifiableSet());
        List<PointTransaction> pointTransactions = originPointTransactions.stream().filter(
                pointTransaction -> paymentIdSet.contains(pointTransaction.getPaymentId())).toList();

        pointTransactions.forEach(PointTransaction::setCancelled);

        log.info("Schedule: Released Payments: {}", payments.stream().map(Payment::getId).toList());
        log.info("Schedule: Released Point transactions: {}", pointTransactions.stream().map(PointTransaction::getId).toList());

        paymentPort.saveAll(payments);
        pointTransactionPort.saveAll(pointTransactions);

        return new Output();
    }
}
