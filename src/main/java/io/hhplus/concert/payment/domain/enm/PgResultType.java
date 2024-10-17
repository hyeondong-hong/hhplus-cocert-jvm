package io.hhplus.concert.payment.domain.enm;

public enum PgResultType {
    OK,
    INVALID,
    ALREADY_PURCHASED,
    REJECTED_PAYMENT,
    EXCEED_REFUNDABLE_DUE,
    REJECTED_REFUND,
}
