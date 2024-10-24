package io.hhplus.concert.app.user.port;

import io.hhplus.concert.app.user.domain.UserPoint;
import io.hhplus.concert.app.user.port.jpa.UserPointJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
public class UserPointPort {

    private final UserPointJpaRepository jpaRepository;

    public UserPoint getByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).orElseThrow();
    }

    public UserPoint getByUserIdWithLock(Long userId) {
        return jpaRepository.findByUserIdWithLock(userId).orElseThrow();
    }

    public UserPoint save(UserPoint userPoint) {
        return jpaRepository.save(userPoint);
    }
}
