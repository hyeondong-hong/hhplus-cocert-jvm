package io.hhplus.concert.app.payment.usecase;

import io.hhplus.concert.app.payment.domain.Payment;
import io.hhplus.concert.app.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.app.payment.port.PaymentPort;
import io.hhplus.concert.app.payment.usecase.dto.PendingPointChargeResult;
import io.hhplus.concert.app.user.domain.PointTransaction;
import io.hhplus.concert.app.user.domain.UserPoint;
import io.hhplus.concert.app.user.domain.enm.PointTransactionStatus;
import io.hhplus.concert.app.user.domain.enm.PointTransactionType;
import io.hhplus.concert.app.user.port.PointTransactionPort;
import io.hhplus.concert.app.user.port.UserPointPort;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@Service
public class PendingChargeUserPointUseCase {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PaymentPort paymentPort;
    private final UserPointPort userPointPort;
    private final PointTransactionPort pointTransactionPort;

    public record Input(
            Long userId,
            BigDecimal amount
    ) { }

    public record Output(
            PendingPointChargeResult pendingPointChargeResult
    ) { }

    @Transactional
    public Output execute(Input input) {
        UserPoint userPoint = userPointPort.getByUserIdWithLock(input.userId());

        Payment payment = paymentPort.save(
                Payment.builder()
                        .userId(input.userId())
                        .price(input.amount())
                        .status(PaymentStatus.PENDING)
                        .dueAt(LocalDateTime.now().plusMinutes(5L))
                        .build()
        );

        pointTransactionPort.save(
                PointTransaction.builder()
                        .userPointId(userPoint.getId())
                        .amount(input.amount().intValue())
                        .type(PointTransactionType.CHARGE)
                        .status(PointTransactionStatus.PENDING)
                        .paymentId(payment.getId())
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build()
        );

        logger.info("Payment for charge point: {}", payment.getPaymentKey());

        return new Output(
                new PendingPointChargeResult(
                        payment.getPaymentKey()
                )
        );
    }
}
