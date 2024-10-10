package io.hhplus.concert.usecase;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SearchConcertUseCase {

    @Data
    public static class Input {
    }
    public record Output(
            Long concertId,
            String title
    ) {}

    public List<Output> execute(Input input) {
        return null;
    }
}
