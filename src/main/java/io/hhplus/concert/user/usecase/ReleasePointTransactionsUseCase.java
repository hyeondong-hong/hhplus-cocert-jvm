package io.hhplus.concert.user.usecase;

import io.hhplus.concert.payment.domain.Payment;
import io.hhplus.concert.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.payment.port.PaymentPort;
import io.hhplus.concert.user.domain.PointTransaction;
import io.hhplus.concert.user.domain.enm.PointTransactionStatus;
import io.hhplus.concert.user.port.PointTransactionPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        // reservation -> payment 순서대로 비관락
        // 다른 트랜잭션 블록에서 payment -> reservation 순서로 처리 시 데드락에 주의
        List<PointTransaction> originPointTransactions = pointTransactionPort.getAllByStatusesWithLock(
                List.of(PointTransactionStatus.PENDING)
        );

        if (!originPointTransactions.isEmpty()) {
            List<Payment> payments = paymentPort.getExpiredAllByIdsWithLock(
                    originPointTransactions.stream().map(PointTransaction::getPaymentId).toList()
            );

            if (!payments.isEmpty()) {
                payments.forEach(payment -> payment.setStatus(PaymentStatus.CANCELLED));

                Set<Long> paymentIdSet = payments.stream().map(Payment::getId).collect(Collectors.toUnmodifiableSet());
                List<PointTransaction> pointTransactions = originPointTransactions.stream().filter(
                        reservation -> paymentIdSet.contains(reservation.getPaymentId())).toList();

                pointTransactions.forEach(reservation -> reservation.setStatus(PointTransactionStatus.CANCELLED));

                paymentPort.saveAll(payments);
                pointTransactionPort.saveAll(pointTransactions);
            }
        }
        return new Output();
    }
}
