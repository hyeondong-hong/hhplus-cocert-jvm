package io.hhplus.concert.app.payment.usecase.dto;

import io.hhplus.concert.app.payment.domain.enm.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseResult(
        Long paymentId,
        Long userId,
        BigDecimal amount,
        PaymentStatus paymentStatus,
        LocalDateTime dueAt,
        LocalDateTime paidAt
) {
}
