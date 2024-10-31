package io.hhplus.concert.app.payment.domain.enm;

import lombok.Getter;

@Getter
public enum PgResultType {
    OK("완료"),
    INVALID("사용 불가"),
    ALREADY_PURCHASED("이미 완료된 결제"),
    REJECTED_PAYMENT("승인 거부"),
    EXCEED_REFUNDABLE_DUE("환불 가능 기한 만료"),
    REJECTED_REFUND("환불 거부");

    private final String description;

    PgResultType(String description) {
        this.description = description;
    }
}
