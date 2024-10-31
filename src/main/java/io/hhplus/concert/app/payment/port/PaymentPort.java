package io.hhplus.concert.app.payment.port;

import io.hhplus.concert.app.payment.domain.Payment;
import io.hhplus.concert.app.payment.port.jpa.PaymentJpaRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
@AllArgsConstructor
@Repository
public class PaymentPort {

    private final PaymentJpaRepository jpaRepository;

    public Payment get(Long id) {
        return jpaRepository.findById(id).orElseThrow();
    }

    public Payment getWithLock(Long id) {
        return jpaRepository.findByIdWithLock(id).orElseThrow();
    }

    public Payment getByPaymentKey(String paymentKey) {
        return jpaRepository.findByPaymentKey(paymentKey).orElseThrow(throwByPaymentKey(paymentKey));
    }

    public Payment getByPaymentKeyWithLock(String paymentKey) {
        return jpaRepository.findByPaymentKeyWithLock(paymentKey).orElseThrow(throwByPaymentKey(paymentKey));
    }

    private Supplier<? extends RuntimeException> throwByPaymentKey(String paymentKey) {
        return () -> {
            log.warn("시스템에 없는 결제키를 조회 시도: paymentKey = {}", paymentKey);
            return new IllegalArgumentException("존재하지 않는 결제키: paymentKey = " + paymentKey);
        };
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
