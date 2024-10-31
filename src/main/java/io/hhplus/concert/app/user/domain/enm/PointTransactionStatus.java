package io.hhplus.concert.app.user.domain.enm;

import lombok.Getter;

@Getter
public enum PointTransactionStatus {
    PENDING("충전 보류 중"),
    COMPLETE("충전 완료"),
    CANCELLED("충전 취소");

    private final String description;

    PointTransactionStatus(String description) {
        this.description = description;
    }
}
