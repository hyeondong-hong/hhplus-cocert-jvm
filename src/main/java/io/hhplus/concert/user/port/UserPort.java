package io.hhplus.concert.user.port;

import io.hhplus.concert.user.domain.User;
import io.hhplus.concert.user.port.jpa.UserJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
public class UserPort {

    private final UserJpaRepository jpaRepository;

    public Boolean isExists(Long userId) {
        return jpaRepository.existsById(userId);
    }

    public User save(User user) {
        return jpaRepository.save(user);
    }

    public User get(Long userId) {
        return jpaRepository.findById(userId).orElseThrow();
    }
}
