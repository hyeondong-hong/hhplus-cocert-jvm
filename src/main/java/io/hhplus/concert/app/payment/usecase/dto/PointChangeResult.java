package io.hhplus.concert.app.payment.usecase.dto;

import io.hhplus.concert.app.user.domain.enm.PointTransactionType;

public record PointChangeResult(
        Long userId,
        Integer remains,
        PointTransactionType transactionType,
        Integer changed
) {
}
