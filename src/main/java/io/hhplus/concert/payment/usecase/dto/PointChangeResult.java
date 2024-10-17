package io.hhplus.concert.payment.usecase.dto;

import io.hhplus.concert.user.domain.enm.PointTransactionType;

public record PointChangeResult(
        Long userId,
        Integer remains,
        PointTransactionType transactionType,
        Integer changed
) {
}
