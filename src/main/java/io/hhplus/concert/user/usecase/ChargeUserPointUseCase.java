package io.hhplus.concert.user.usecase;

import lombok.Data;
import org.springframework.stereotype.Service;

@Service
public class ChargeUserPointUseCase {

    @Data
    public static class Input {
        private Integer amount;
    }
    public record Output(
            Long userId,
            Integer remains,
            Integer charged
    ) {}

    public Output execute(Input input) {
        return null;
    }
}
