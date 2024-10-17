package io.hhplus.concert.concert.usecase;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchSeatsUseCase {

    @Data
    public static class Input {
    }
    public record Output(
            Long concertScheduleId,
            List<Integer> seatNumbers
    ) {}

    public Output execute(Input input) {
        return null;
    }
}
