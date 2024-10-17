package io.hhplus.concert.user.usecase;

import io.hhplus.concert.user.domain.UserPoint;
import io.hhplus.concert.user.port.UserPointPort;
import io.hhplus.concert.user.port.UserPort;
import io.hhplus.concert.user.usecase.dto.UserPointResult;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class RetrieveUserPointUseCase {

    private final UserPointPort userPointPort;
    private final UserPort userPort;

    public RetrieveUserPointUseCase(UserPointPort userPointPort, UserPort userPort) {
        this.userPointPort = userPointPort;
        this.userPort = userPort;
    }

    public record Input(
            Long userId
    ) { }

    public record Output(
            UserPointResult userPointResult
    ) { }

    public Output execute(Input input) {
        if (!userPort.isExists(input.userId())) {
            throw new NoSuchElementException("User not found: userId = " + input.userId());
        }
        UserPoint userPoint = userPointPort.getByUserId(input.userId());
        return new Output(
                new UserPointResult(
                        userPoint.getUserId(),
                        userPoint.getRemains()
                )
        );
    }
}
