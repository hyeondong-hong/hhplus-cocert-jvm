package io.hhplus.concert.user.port.jpa;

import io.hhplus.concert.user.domain.UserPoint;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserPointJpaRepository extends JpaRepository<UserPoint, Long> {

    Optional<UserPoint> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM UserPoint p WHERE p.userId = :userId")
    Optional<UserPoint> findByUserIdWithLock(Long userId);
}
