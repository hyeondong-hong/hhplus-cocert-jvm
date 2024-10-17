package io.hhplus.concert.integration.user.usecase;

import io.hhplus.concert.user.domain.Token;
import io.hhplus.concert.user.port.TokenPort;
import io.hhplus.concert.user.port.UserPointPort;
import io.hhplus.concert.user.port.UserPort;
import io.hhplus.concert.user.usecase.IssueTokenUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class IssueTokenUseCaseIntegrationTest {

    @Autowired
    private IssueTokenUseCase issueTokenUseCase;

    @Autowired
    private TokenPort tokenPort;

    @Autowired
    private UserPort userPort;

    @Autowired
    private UserPointPort userPointPort;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
