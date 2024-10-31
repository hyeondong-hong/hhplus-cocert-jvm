package io.hhplus.concert.app.payment.domain.enm;

import lombok.Getter;

@Getter
public enum PaymentTransactionStatus {
    PURCHASE("결제"),
    REFUND("환불");

    private final String description;

    PaymentTransactionStatus(String description) {
        this.description = description;
    }
}
