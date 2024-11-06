package io.hhplus.concert.app.payment.port.jpa;

import io.hhplus.concert.app.payment.domain.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id = :id")
    Optional<Payment> findByIdWithLock(Long id);

    Optional<Payment> findByPaymentKey(String paymentKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.paymentKey = :paymentKey")
    Optional<Payment> findByPaymentKeyWithLock(String paymentKey);

    List<Payment> findAllByIdInAndDueAtLessThan(Collection<Long> ids, LocalDateTime baseDateTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id in :ids and p.dueAt < :baseDateTime")
    List<Payment> findAllByIdInAndDueAtLessThanWithLock(Collection<Long> ids, LocalDateTime baseDateTime);

    List<Payment> findAllByIdInAndDueAtGreaterThanEqual(Collection<Long> ids, LocalDateTime baseDateTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id in :ids and p.dueAt >= :baseDateTime")
    List<Payment> findAllByIdInAndDueAtGreaterThanEqualWithLock(Collection<Long> ids, LocalDateTime baseDateTime);
}
