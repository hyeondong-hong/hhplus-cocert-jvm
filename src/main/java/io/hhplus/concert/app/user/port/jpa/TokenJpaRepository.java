package io.hhplus.concert.app.user.port.jpa;

import io.hhplus.concert.app.user.domain.Token;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenJpaRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByKeyUuid(String key);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Token t WHERE t.keyUuid = :keyUuid")
    Optional<Token> findByKeyUuidWithLock(String keyUuid);

    Boolean existsByUserId(Long userId);

    Optional<Token> findByUserId(Long userId);

    List<Long> findAllIdsByExpiresAtLessThan(LocalDateTime baseDateTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t.id FROM Token t WHERE t.expiresAt < :baseDateTime")
    List<Long> findAllIdsByExpiresAtLessThanWithLock(LocalDateTime baseDateTime);
}
