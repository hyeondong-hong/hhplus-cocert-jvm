package io.hhplus.concert.app.user.usecase;

import io.hhplus.concert.app.user.domain.UserPoint;
import io.hhplus.concert.app.user.port.UserPointPort;
import io.hhplus.concert.app.user.port.UserPort;
import io.hhplus.concert.app.user.usecase.dto.UserPointResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class RetrieveUserPointUseCase {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
            logger.warn("미등록 유저가 포인트에 접근 시도: userId = {}", input.userId());
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
