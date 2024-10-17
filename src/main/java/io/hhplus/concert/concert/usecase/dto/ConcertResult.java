package io.hhplus.concert.concert.usecase.dto;

public record ConcertResult(
        Long concertId,
        String title,
        String cast
) {
}
