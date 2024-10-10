package io.hhplus.concert.usecase.concert;

import lombok.Data;
import org.springframework.stereotype.Service;

@Service
public class PurchaseReservationUseCase {

    @Data
    public static class Input {
        private Long userId;
    }
    public record Output(
            Long reservationId,
            Long userId,
            Long paymentId,
            String paymentStatus
    ) {}

    public Output execute(Input input) {
        return null;
    }
}
