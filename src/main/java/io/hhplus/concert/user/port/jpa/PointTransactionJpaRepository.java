package io.hhplus.concert.user.port.jpa;

import io.hhplus.concert.payment.domain.enm.PaymentTransactionStatus;
import io.hhplus.concert.user.domain.PointTransaction;
import io.hhplus.concert.user.domain.enm.PointTransactionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PointTransactionJpaRepository extends JpaRepository<PointTransaction, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<PointTransaction> findAllByStatusIn(Collection<PointTransactionStatus> statuses);

    Optional<PointTransaction> findByPaymentId(Long paymentId);
}
