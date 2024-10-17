package io.hhplus.concert.payment.port;

import io.hhplus.concert.payment.domain.enm.PgResultType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HangHaePgPort {

    public PgResultType purchase(Long userId, String paymentKey, BigDecimal amount) {
        // stub
        return PgResultType.OK;
    }

    public PgResultType refund(Long userId, String paymentKey, BigDecimal amount) {
        // stub
        return PgResultType.OK;
    }
}
