package io.hhplus.concert.app.concert.usecase.dto;

import java.time.LocalDateTime;

public record ConcertScheduleResult(
        Long concertId,
        Long concertScheduleId,
        LocalDateTime scheduledAt
) {
}
