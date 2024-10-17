package io.hhplus.concert.user.usecase;

import io.hhplus.concert.user.domain.Token;
import io.hhplus.concert.user.domain.User;
import io.hhplus.concert.user.domain.UserPoint;
import io.hhplus.concert.user.port.TokenPort;
import io.hhplus.concert.user.port.UserPointPort;
import io.hhplus.concert.user.port.UserPort;
import io.hhplus.concert.user.usecase.dto.TokenResult;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@AllArgsConstructor
@Service
public class IssueTokenUseCase {

    private final TokenPort tokenPort;
    private final UserPort userPort;
    private final UserPointPort userPointPort;
    private final PasswordEncoder passwordEncoder;

    public record Input(
            Long userId
    ) { }

    public record Output(
            TokenResult tokenResult
    ) { }

    @Transactional
    public Output execute(Input input) {
        Long userId = input.userId();

        if (!userPort.isExists(userId)) {
            User user = new User();
            user.setId(userId);
            user.setUsername("유저 " + userId);
            user.setPassword(passwordEncoder.encode("passFor" + userId));
            user.setCreatedAt(LocalDateTime.now());
            userPort.save(user);

            UserPoint userPoint = new UserPoint();
            userPoint.setUserId(userId);
            userPointPort.save(userPoint);
        }

        Token token = new Token();
        token.setUserId(userId);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(token.getCreatedAt().plusDays(1L));
        token = tokenPort.save(token);

        return new Output(new TokenResult(token.getKeyUuid()));
    }
}
