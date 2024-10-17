package io.hhplus.concert.payment.usecase.dto;

import io.hhplus.concert.payment.domain.enm.PaymentStatus;

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
