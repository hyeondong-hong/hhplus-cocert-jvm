package io.hhplus.concert.user.port;

import io.hhplus.concert.user.domain.PointTransaction;
import io.hhplus.concert.user.domain.enm.PointTransactionStatus;
import io.hhplus.concert.user.port.jpa.PointTransactionJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@Repository
public class PointTransactionPort {

    private final PointTransactionJpaRepository jpaRepository;

    public PointTransaction getByPaymentId(Long paymentId) {
        return jpaRepository.findByPaymentId(paymentId).orElseThrow();
    }

    public List<PointTransaction> getAllByStatusesWithLock(Collection<PointTransactionStatus> statuses) {
        return jpaRepository.findAllByStatusIn(statuses);
    }

    public PointTransaction save(PointTransaction pointTransaction) {
        return jpaRepository.save(pointTransaction);
    }

    public List<PointTransaction> saveAll(Iterable<PointTransaction> pointTransactions) {
        return jpaRepository.saveAll(pointTransactions);
    }

    public void delete(PointTransaction pointTransaction) {
        jpaRepository.delete(pointTransaction);
    }
}
