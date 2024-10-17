package io.hhplus.concert.user.port;

import io.hhplus.concert.user.domain.Token;
import io.hhplus.concert.user.port.jpa.TokenJpaRepository;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Repository
public class TokenPort {

    private final TokenJpaRepository jpaRepository;

    public Token getByKey(@NotNull String keyUuid) {
        return jpaRepository.findByKeyUuid(keyUuid).orElseThrow(
                () -> new BadCredentialsException("존재하지 않는 토큰: keyUuid = " + keyUuid));
    }

    public Token getByKeyWithLock(@NotNull String keyUuid) {
        return jpaRepository.findByKeyUuidWithLock(keyUuid).orElseThrow(
                () -> new BadCredentialsException("존재하지 않는 토큰: key = " + keyUuid));
    }

    public Boolean existsByUserId(@NotNull Long userId) {
        return jpaRepository.existsByUserId(userId);
    }

    public Token save(@NotNull Token token) {
        return jpaRepository.save(token);
    }

    public List<Long> findAllIdsExpiredWithLock() {
        return jpaRepository.findAllIdsByExpiresAtLessThan(LocalDateTime.now());
    }

    public void deleteAll(Iterable<Token> tokens) {
        jpaRepository.deleteAll(tokens);
    }
}
