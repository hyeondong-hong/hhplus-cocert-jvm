package io.hhplus.concert.concert.port;

import io.hhplus.concert.concert.domain.Concert;
import io.hhplus.concert.concert.port.jpa.ConcertJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
public class ConcertPort {

    private final ConcertJpaRepository jpaRepository;

    public Boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    public Page<Concert> findAllByPageable(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }

    public Concert save(Concert concert) {
        return jpaRepository.save(concert);
    }
}
