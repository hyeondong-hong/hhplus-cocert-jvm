package io.hhplus.concert.integration.user.usecase;

import io.hhplus.concert.user.domain.User;
import io.hhplus.concert.user.domain.UserPoint;
import io.hhplus.concert.user.port.UserPointPort;
import io.hhplus.concert.user.port.UserPort;
import io.hhplus.concert.user.usecase.RetrieveUserPointUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class RetrieveUserPointUseCaseIntegrationTest {

    @Autowired
    private RetrieveUserPointUseCase retrieveUserPointUseCase;

    @Autowired
    private UserPointPort userPointPort;

    @Autowired
    private UserPort userPort;

    private User user;
    private UserPoint userPoint;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setUsername("테스트 유저");
        user.setPassword("<PASSWORD>");
        user.setCreatedAt(LocalDateTime.now());
        user = userPort.save(user);
        userPoint = new UserPoint();
        userPoint.setUserId(user.getId());
        userPoint.setRemains(5000);
        userPointPort.save(userPoint);
    }

    @Test
    @DisplayName("유저 ID로 해당 유저의 포인트를 조회한다")
    public void retrieveUserPoint() {
        RetrieveUserPointUseCase.Output output = retrieveUserPointUseCase.execute(
                new RetrieveUserPointUseCase.Input(
                        user.getId()
                )
        );

        assertEquals(output.userPointResult().remains(), userPoint.getRemains());
    }
}
