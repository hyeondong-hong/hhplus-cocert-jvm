package io.hhplus.concert.user.usecase;

import io.hhplus.concert.user.domain.ServiceEntry;
import io.hhplus.concert.user.domain.Token;
import io.hhplus.concert.user.port.ServiceEntryPort;
import io.hhplus.concert.user.port.TokenPort;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@AllArgsConstructor
@Service
public class CheckTokenEnrolledUseCase {

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

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Token Expired");
        }

        ServiceEntry serviceEntry;
        if (serviceEntryPort.exists(token.getId())) {
            // 등록된 토큰인 경우 불러오기
            serviceEntry = serviceEntryPort.getByTokenId(token.getId());
        } else {
            // 미등록 토큰인 경우 등록
            serviceEntry = new ServiceEntry();
            serviceEntry.setTokenId(token.getId());
            serviceEntry.setEntryAt(LocalDateTime.now());
            serviceEntry = serviceEntryPort.save(serviceEntry);
        }

        boolean isAuthenticated = true;
        Long rank = null;

        if (serviceEntry.getEnrolledAt() == null) {
            // 미진입 상태 -> 403
            isAuthenticated = false;
            rank = serviceEntryPort.getEntryRankByTokenId(token.getId());
        }

        return new Output(
                isAuthenticated,
                rank
        );
    }
}
