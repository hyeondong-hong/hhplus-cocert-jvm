package io.hhplus.concert.app.payment.port.api;

import io.hhplus.concert.app.payment.domain.enm.PgResultType;

import java.math.BigDecimal;

public interface PgPort {

    PgResultType purchase(Long userId, String paymentKey, BigDecimal amount);

    PgResultType refund(Long userId, String paymentKey, BigDecimal amount);
}
