package io.hhplus.concert.concert.usecase;

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
            Long paymentId,
            String paymentStatus
    ) {}

    public Output execute(Input input) {
        return null;
    }
}
