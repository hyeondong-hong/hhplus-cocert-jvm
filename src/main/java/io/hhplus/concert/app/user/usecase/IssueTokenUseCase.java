package io.hhplus.concert.app.user.usecase;

import io.hhplus.concert.app.user.domain.Token;
import io.hhplus.concert.app.user.domain.User;
import io.hhplus.concert.app.user.domain.UserPoint;
import io.hhplus.concert.app.user.port.TokenPort;
import io.hhplus.concert.app.user.port.UserPointPort;
import io.hhplus.concert.app.user.port.UserPort;
import io.hhplus.concert.app.user.usecase.dto.TokenResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@AllArgsConstructor
@Service
public class IssueTokenUseCase {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
        LocalDateTime currentDateTime = LocalDateTime.now();

        if (!userPort.isExists(userId)) {
            logger.info("User created: userId = {}", userId);
            userPort.save(
                    User.builder()
                            .id(userId)
                            .username("유저 " + userId)
                            .password(passwordEncoder.encode("passFor" + userId))
                            .createdAt(currentDateTime)
                            .build()
            );

            userPointPort.save(
                    UserPoint.builder()
                            .remains(0)
                            .userId(userId)
                            .build()
            );
        }

        Token token = tokenPort.save(
                Token.builder()
                        .userId(userId)
                        .createdAt(currentDateTime)
                        .expiresAt(currentDateTime.plusDays(1L))
                        .build()
        );
        logger.info("Token created: uuid = {}", token.getKeyUuid());

        return new Output(new TokenResult(token.getKeyUuid()));
    }
}
