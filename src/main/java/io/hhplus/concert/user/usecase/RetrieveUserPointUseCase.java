package io.hhplus.concert.user.usecase;

import lombok.Data;
import org.springframework.stereotype.Service;

@Service
public class RetrieveUserPointUseCase {

    @Data
    public static class Input {
    }
    public record Output(
            Long userId,
            Integer remains
    ) {}

    public Output execute(Input input) {
        return null;
    }
}
