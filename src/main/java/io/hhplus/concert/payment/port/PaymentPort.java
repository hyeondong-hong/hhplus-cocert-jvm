package io.hhplus.concert.payment.port;

import io.hhplus.concert.payment.domain.Payment;
import io.hhplus.concert.payment.port.jpa.PaymentJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Repository
public class PaymentPort {

    private final PaymentJpaRepository jpaRepository;

    public Payment get(Long id) {
        return jpaRepository.findById(id).orElseThrow();
    }

    public Payment getByPaymentKey(String paymentKey) {
        return jpaRepository.findByPaymentKey(paymentKey).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 결제키: paymentKey = " + paymentKey));
    }

    public Payment getWithLock(Long id) {
        return jpaRepository.findByIdWithLock(id).orElseThrow();
    }

    public Payment getByPaymentKeyWithLock(String paymentKey) {
        return jpaRepository.findByPaymentKeyWithLock(paymentKey).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 결제키: paymentKey = " + paymentKey));
    }

    public Payment save(Payment payment) {
        return jpaRepository.save(payment);
    }

    public List<Payment> saveAll(List<Payment> payments) {
        return jpaRepository.saveAll(payments);
    }

    public void delete(Payment payment) {
        jpaRepository.delete(payment);
    }

    public List<Payment> getExpiredAllByIdsWithLock(List<Long> ids) {
        return jpaRepository.findAllByIdInAndDueAtLessThan(ids, LocalDateTime.now());
    }

    public List<Payment> getAvailableAllByIdsWithLock(List<Long> ids) {
        return jpaRepository.findAllByIdInAndDueAtGreaterThanEqual(ids, LocalDateTime.now());
    }
}
