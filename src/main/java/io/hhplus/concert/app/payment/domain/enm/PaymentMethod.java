package io.hhplus.concert.app.payment.domain.enm;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CASH("현금 결제"),
    POINT("포인트 결제");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }
}
