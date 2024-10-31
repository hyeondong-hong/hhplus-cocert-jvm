package io.hhplus.concert.apps.integration.user.usecase;

import io.hhplus.concert.app.user.domain.Token;
import io.hhplus.concert.app.user.port.TokenPort;
import io.hhplus.concert.app.user.usecase.IssueTokenUseCase;
import io.hhplus.concert.config.IntegrationTestService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class IssueTokenUseCaseIntegrationTest {

    @Autowired
    private IntegrationTestService integrationTestService;

    @Autowired
    private IssueTokenUseCase issueTokenUseCase;

    @Autowired
    private TokenPort tokenPort;

    @AfterEach
    public void tearDown() {
        integrationTestService.tearDown();
    }

    @Test
    @DisplayName("유저 id로 조회 시 토큰이 발행된다")
    public void issueToken() {
        IssueTokenUseCase.Output output = issueTokenUseCase.execute(
                new IssueTokenUseCase.Input(
                        1L
                )
        );
        String uuid = output.tokenResult().keyUuid();
        Token token = tokenPort.getByKey(uuid);
        assertEquals(1L, token.getUserId());
    }
}
