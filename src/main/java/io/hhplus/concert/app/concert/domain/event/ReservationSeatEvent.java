package io.hhplus.concert.app.concert.domain.event;

public record ReservationSeatEvent(
        Long userId,
        Long concertSeatId
) {
}
