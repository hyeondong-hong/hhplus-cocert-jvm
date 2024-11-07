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
import io.hhplus.concert.config.aop.annotation.RedisLock;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@AllArgsConstructor
@Service
public class PendingChargeUserPointUseCase {

    private final PaymentPort paymentPort;
    private final UserPointPort userPointPort;
    private final PointTransactionPort pointTransactionPort;

    public record Input(
            String keyUuid,
            Long userId,
            BigDecimal amount
    ) { }

    public record Output(
            PendingPointChargeResult pendingPointChargeResult
    ) { }

    @RedisLock(transactional = true, key = "Point", dtoName = "input", fields = {"keyUuid"})
    public Output execute(Input input) {
        UserPoint userPoint = userPointPort.getByUserId(input.userId());

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

        log.info("Payment for charge point: {}", payment.getPaymentKey());

        return new Output(
                new PendingPointChargeResult(
                        payment.getPaymentKey()
                )
        );
    }
}
