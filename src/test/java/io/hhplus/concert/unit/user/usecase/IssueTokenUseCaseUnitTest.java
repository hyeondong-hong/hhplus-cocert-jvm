package io.hhplus.concert.unit.user.usecase;

import io.hhplus.concert.app.user.domain.Token;
import io.hhplus.concert.app.user.domain.User;
import io.hhplus.concert.app.user.domain.UserPoint;
import io.hhplus.concert.app.user.port.TokenPort;
import io.hhplus.concert.app.user.port.UserPointPort;
import io.hhplus.concert.app.user.port.UserPort;
import io.hhplus.concert.app.user.usecase.IssueTokenUseCase;
import io.hhplus.concert.app.user.usecase.dto.TokenResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IssueTokenUseCaseUnitTest {

    @Mock
    private TokenPort tokenPort;

    @Mock
    private UserPort userPort;

    @Mock
    private UserPointPort userPointPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private IssueTokenUseCase issueTokenUseCase;

    private final List<User> userCreated = new ArrayList<>();
    private final List<UserPoint> userPointCreated = new ArrayList<>();
    private final List<Token> tokenCreated = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        lenient().when(passwordEncoder.encode(any(String.class))).thenReturn("< Encoded Password >");
        lenient().when(userPort.save(any(User.class))).then(r -> {
            userCreated.add(r.getArgument(0));
            return r.getArgument(0);
        });
        lenient().when(userPointPort.save(any(UserPoint.class))).then(r -> {
            userPointCreated.add(r.getArgument(0));
            return r.getArgument(0);
        });
        when(tokenPort.save(any(Token.class))).then(r -> {
            tokenCreated.add(r.getArgument(0));
            return r.getArgument(0);
        });
    }

    @AfterEach
    public void tearDown() {
        userCreated.clear();
        userPointCreated.clear();
        tokenCreated.clear();
    }

    @Test
    @DisplayName("유저가 없을 때 유저와 유저 포인트를 생성하고 토큰을 발행한다")
    public void noUser() {
        when(userPort.isExists(any(Long.class))).thenReturn(false);

        IssueTokenUseCase.Output output = issueTokenUseCase.execute(new IssueTokenUseCase.Input(
                10L
        ));
        TokenResult result = output.tokenResult();

        assertEquals(1, userCreated.size());
        assertEquals(1, userPointCreated.size());
        assertEquals(1, tokenCreated.size());
        assertEquals(tokenCreated.get(0).getKeyUuid(), result.keyUuid());
    }

    @Test
    @DisplayName("유저가 있다면 토큰만 발행한다")
    public void existsUser() {
        when(userPort.isExists(any(Long.class))).thenReturn(true);

        IssueTokenUseCase.Output output = issueTokenUseCase.execute(new IssueTokenUseCase.Input(
                10L
        ));
        TokenResult result = output.tokenResult();

        assertEquals(0, userCreated.size());
        assertEquals(0, userPointCreated.size());
        assertEquals(1, tokenCreated.size());
        assertEquals(tokenCreated.get(0).getKeyUuid(), result.keyUuid());
    }
}
