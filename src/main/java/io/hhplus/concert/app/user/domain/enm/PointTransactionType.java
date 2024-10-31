package io.hhplus.concert.app.user.domain.enm;

import lombok.Getter;

@Getter
public enum PointTransactionType {
    CHARGE("충전"),
    USED("사용");

    private final String description;

    PointTransactionType(String description) {
        this.description = description;
    }
}
