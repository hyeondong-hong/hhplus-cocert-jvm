package io.hhplus.concert.usecase.concert;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SearchSchedulesUseCase {

    @Data
    public static class Input {
    }
    public record Output(
            Long concertId,
            Long concertScheduleId,
            LocalDateTime scheduledAt
    ) {}

    public List<Output> execute(Input input) {
        return null;
    }
}
