package io.hhplus.concert.app.concert.usecase.dto;

import java.math.BigDecimal;

public record ConcertSeatResult(
        Long concertId,
        Long concertScheduleId,
        Long concertSeatId,
        String label,
        Integer seatNumber,
        Boolean isActive,
        BigDecimal price
) {
}
