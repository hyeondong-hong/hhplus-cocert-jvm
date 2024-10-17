package io.hhplus.concert.concert.port;

import io.hhplus.concert.concert.domain.Concert;
import io.hhplus.concert.concert.domain.ConcertSchedule;
import io.hhplus.concert.concert.port.jpa.ConcertScheduleJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Repository
public class ConcertSchedulePort {

    private final ConcertScheduleJpaRepository jpaRepository;

    public Boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    public Page<ConcertSchedule> findAllByConcertIdAndPageable(Long concertId, Pageable pageable) {
        return jpaRepository.findAllByConcertId(concertId, pageable);
    }

    public List<Long> getAvailableAllIdsByConcertId(Long concertId) {
        return jpaRepository.findAllIdsByConcertIdAndScheduledAtGreaterThanEqual(concertId, LocalDateTime.now());
    }

    public ConcertSchedule save(ConcertSchedule concertSchedule) {
        return jpaRepository.save(concertSchedule);
    }
}
