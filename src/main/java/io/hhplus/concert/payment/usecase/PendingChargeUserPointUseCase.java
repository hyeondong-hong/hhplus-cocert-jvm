package io.hhplus.concert.payment.usecase;

import io.hhplus.concert.payment.domain.Payment;
import io.hhplus.concert.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.payment.port.PaymentPort;
import io.hhplus.concert.payment.usecase.dto.PendingPointChargeResult;
import io.hhplus.concert.user.domain.PointTransaction;
import io.hhplus.concert.user.domain.UserPoint;
import io.hhplus.concert.user.domain.enm.PointTransactionStatus;
import io.hhplus.concert.user.domain.enm.PointTransactionType;
import io.hhplus.concert.user.port.PointTransactionPort;
import io.hhplus.concert.user.port.UserPointPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@Service
public class PendingChargeUserPointUseCase {

    private final PaymentPort paymentPort;
    private final UserPointPort userPointPort;
    private final PointTransactionPort pointTransactionPort;

    public record Input(
            Long userId,
            BigDecimal amount
    ) { }

    public record Output(
            PendingPointChargeResult pendingPointChargeResult
    ) {}

    @Transactional
    public Output execute(Input input) {
        UserPoint userPoint = userPointPort.getByUserIdWithLock(input.userId());

        Payment payment = new Payment();
        payment.setUserId(input.userId());
        payment.setPrice(input.amount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setDueAt(LocalDateTime.now().plusMinutes(5L));
        payment = paymentPort.save(payment);

        PointTransaction pointTransaction = new PointTransaction();
        pointTransaction.setUserPointId(userPoint.getId());
        pointTransaction.setAmount(input.amount().intValue());
        pointTransaction.setType(PointTransactionType.CHARGE);
        pointTransaction.setStatus(PointTransactionStatus.PENDING);
        pointTransaction.setPaymentId(payment.getId());
        pointTransaction.setCreatedAt(LocalDateTime.now());
        pointTransaction.setModifiedAt(LocalDateTime.now());
        pointTransactionPort.save(pointTransaction);

        return new Output(
                new PendingPointChargeResult(
                        payment.getPaymentKey()
                )
        );
    }
}
