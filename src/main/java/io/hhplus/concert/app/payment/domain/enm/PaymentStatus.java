package io.hhplus.concert.app.payment.domain.enm;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING("결제 보류 중"),
    PAID("결제 완료"),
    REFUNDED("환불됨"),
    CANCELLED("결제 취소됨");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }
}
