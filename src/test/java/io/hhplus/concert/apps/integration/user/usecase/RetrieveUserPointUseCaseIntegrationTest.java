package io.hhplus.concert.apps.integration.user.usecase;

import io.hhplus.concert.app.user.domain.User;
import io.hhplus.concert.app.user.domain.UserPoint;
import io.hhplus.concert.app.user.port.UserPointPort;
import io.hhplus.concert.app.user.port.UserPort;
import io.hhplus.concert.app.user.usecase.RetrieveUserPointUseCase;
import io.hhplus.concert.config.IntegrationTestService;
import org.junit.jupiter.api.AfterEach;
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
    private IntegrationTestService integrationTestService;

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
        user = userPort.save(
                User.builder()
                        .username("테스트 유저")
                        .password("<PASSWORD>")
                        .createdAt(LocalDateTime.now())
                        .build()
        );
        userPoint = userPointPort.save(
                UserPoint.builder()
                        .userId(user.getId())
                        .remains(5000)
                        .build()
        );
    }

    @AfterEach
    public void tearDown() {
        integrationTestService.tearDown();
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
