package io.hhplus.concert.usecase.concert;

import lombok.Data;
import org.springframework.stereotype.Service;

@Service
public class ReservationSeatUseCase {

    @Data
    public static class Input {
        private Long userId;
        private String token;
    }
    public record Output(
            Long reservationId,
            Long concertScheduleId,
            Integer seatNumber,
            String reservationStatus,
            Long paymentId,
            String paymentStatus
    ) {}

    public Output execute(Input input) {
        return null;
    }
}
