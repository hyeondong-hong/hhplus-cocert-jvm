package io.hhplus.concert.app.concert.port;

import io.hhplus.concert.app.concert.domain.Concert;
import io.hhplus.concert.app.concert.port.jpa.ConcertJpaRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
public class ConcertPort {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ConcertJpaRepository jpaRepository;

    public Boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    public void existsOrThrow(Long id) {
        if (!existsById(id)) {
            logger.warn("유효하지 않은 콘서트에 접근: concertId = {}", id);
            throw new IllegalArgumentException("Concert not found: " + id);
        }
    }

    public Page<Concert> findAllByPageable(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }

    public Concert save(Concert concert) {
        return jpaRepository.save(concert);
    }
}