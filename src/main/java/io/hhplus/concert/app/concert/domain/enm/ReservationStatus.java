package io.hhplus.concert.app.concert.domain.enm;

import lombok.Getter;

@Getter
public enum ReservationStatus {
    PENDING("예약 대기"),
    COMPLETE("예약 완료"),
    CANCELLED("예약 취소");

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }

}
