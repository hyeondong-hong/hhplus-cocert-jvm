package io.hhplus.concert.app.payment.domain.enm;

public enum PgResultType {
    OK,
    INVALID,
    ALREADY_PURCHASED,
    REJECTED_PAYMENT,
    EXCEED_REFUNDABLE_DUE,
    REJECTED_REFUND,
}
