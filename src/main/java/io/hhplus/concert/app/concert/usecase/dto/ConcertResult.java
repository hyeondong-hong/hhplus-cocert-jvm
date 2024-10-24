package io.hhplus.concert.app.concert.usecase.dto;

public record ConcertResult(
        Long concertId,
        String title,
        String cast
) {
}
