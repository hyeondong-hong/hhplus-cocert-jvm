package io.hhplus.concert.app.payment.port;

import io.hhplus.concert.app.payment.domain.PaymentTransaction;
import io.hhplus.concert.app.payment.port.jpa.PaymentTransactionJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
public class PaymentTransactionPort {

    private final PaymentTransactionJpaRepository jpaRepository;

    public PaymentTransaction getByPaymentId(Long paymentId) {
        return jpaRepository.findByPaymentId(paymentId).orElseThrow();
    }

    public PaymentTransaction getByPaymentIdWithLock(Long paymentId) {
        return jpaRepository.findByPaymentIdWithLock(paymentId).orElseThrow();
    }

    public PaymentTransaction save(PaymentTransaction paymentTransaction) {
        return jpaRepository.save(paymentTransaction);
    }

    public void delete(PaymentTransaction paymentTransaction) {
        jpaRepository.delete(paymentTransaction);
    }
}
