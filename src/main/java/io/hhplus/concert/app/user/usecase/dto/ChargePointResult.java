package io.hhplus.concert.app.user.usecase.dto;

import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record ChargePointResult(
        @Min(1) BigDecimal chargeAmount
) {
}
