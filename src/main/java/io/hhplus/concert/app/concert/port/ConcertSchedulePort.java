package io.hhplus.concert.app.concert.port;

import io.hhplus.concert.app.concert.domain.ConcertSchedule;
import io.hhplus.concert.app.concert.port.jpa.ConcertScheduleJpaRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Repository
public class ConcertSchedulePort {

    private final ConcertScheduleJpaRepository jpaRepository;

    public Boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    public void existsOrThrow(Long id) {
        if (!existsById(id)) {
            log.warn("유효하지 않은 콘서트 스케줄에 접근: concertScheduleId = {}", id);
            throw new IllegalArgumentException("Concert Schedule not found: " + id);
        }
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
