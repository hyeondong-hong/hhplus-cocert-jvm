package io.hhplus.concert.app.user.port;

import io.hhplus.concert.app.user.domain.User;
import io.hhplus.concert.app.user.port.jpa.UserJpaRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.NoSuchElementException;

@Slf4j
@AllArgsConstructor
@Repository
public class UserPort {

    private final UserJpaRepository jpaRepository;

    public Boolean isExists(Long id) {
        return jpaRepository.existsById(id);
    }

    public void existsOrThrow(Long id) {
        if (!isExists(id)) {
            log.warn("존재하지 않는 유저: userId = {}", id);
            throw new NoSuchElementException("User not found: userId = " + id);
        }
    }

    public User get(Long id) {
        return jpaRepository.findById(id).orElseThrow();
    }

    public User save(User user) {
        return jpaRepository.save(user);
    }
}
