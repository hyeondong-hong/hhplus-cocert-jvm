package io.hhplus.concert.concert.usecase.dto;

import io.hhplus.concert.concert.domain.enm.ReservationStatus;

public record ReservationResult(
        Long reservationId,
        Long userId,
        Long concertId,
        Long concertScheduleId,
        Long concertSeatId,
        ReservationStatus status,
        Integer seatNumber,
        String paymentKey
) {
}
