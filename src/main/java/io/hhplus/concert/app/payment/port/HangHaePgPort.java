package io.hhplus.concert.app.payment.port;

import io.hhplus.concert.app.payment.domain.enm.PgResultType;
import io.hhplus.concert.app.payment.port.api.PgPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HangHaePgPort implements PgPort {

    public PgResultType purchase(Long userId, String paymentKey, BigDecimal amount) {
        // stub
        return PgResultType.OK;
    }

    public PgResultType refund(Long userId, String paymentKey, BigDecimal amount) {
        // stub
        return PgResultType.OK;
    }
}
