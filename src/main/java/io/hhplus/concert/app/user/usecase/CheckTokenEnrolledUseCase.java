package io.hhplus.concert.app.user.usecase;

import io.hhplus.concert.app.user.domain.ServiceEntry;
import io.hhplus.concert.app.user.domain.Token;
import io.hhplus.concert.app.user.port.ServiceEntryPort;
import io.hhplus.concert.app.user.port.TokenPort;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@AllArgsConstructor
@Service
public class CheckTokenEnrolledUseCase {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ServiceEntryPort serviceEntryPort;
    private final TokenPort tokenPort;

    public record Input(
            String keyUuid
    ) { }

    public record Output(
            Boolean isAuthenticated,
            Long rank
    ) { }

    @Transactional
    public Output execute(Input input) {
        // 토큰 조회 (없을 경우 BadCredentialsException -> 401)
        Token token = tokenPort.getByKey(input.keyUuid());

        if (token.isExpired()) {
            logger.debug("Token expired: uuid = {}", token.getKeyUuid());
            throw new BadCredentialsException("Token Expired");
        }

        ServiceEntry serviceEntry;
        if (serviceEntryPort.existsByTokenId(token.getId())) {
            // 등록된 토큰인 경우 불러오기
            logger.debug("등록된 토큰: token = {}", token.getKeyUuid());
            serviceEntry = serviceEntryPort.getByTokenId(token.getId());
        } else {
            // 미등록 토큰인 경우 등록
            logger.debug("미등록 토큰: token = {}", token.getKeyUuid());
            serviceEntry = serviceEntryPort.save(
                    ServiceEntry.builder()
                            .tokenId(token.getId())
                            .entryAt(LocalDateTime.now())
                            .build()
            );
        }

        boolean isAuthenticated = true;
        Long rank = null;

        if (!serviceEntry.isEnrolled()) {
            // 미진입 상태 -> 403
            logger.debug("진입시도 토큰: {}", token.getKeyUuid());
            isAuthenticated = false;
            rank = serviceEntryPort.getEntryRankByTokenId(token.getId());
        }

        return new Output(
                isAuthenticated,
                rank
        );
    }
}
