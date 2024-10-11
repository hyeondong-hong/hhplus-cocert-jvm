package io.hhplus.concert.usecase.user;

import lombok.Data;
import org.springframework.stereotype.Service;

@Service
public class IssueTokenUseCase {

    @Data
    public static class Input {
    }
    public record Output(String token) {}

    public Output execute(Input input) {
        return null;
    }
}
