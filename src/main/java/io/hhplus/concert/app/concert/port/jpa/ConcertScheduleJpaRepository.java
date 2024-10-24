package io.hhplus.concert.app.concert.port.jpa;

import io.hhplus.concert.app.concert.domain.ConcertSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConcertScheduleJpaRepository extends JpaRepository<ConcertSchedule, Long> {

    Page<ConcertSchedule> findAllByConcertId(Long concertId, Pageable pageable);
    List<Long> findAllIdsByConcertIdAndScheduledAtGreaterThanEqual(Long concertId, LocalDateTime baseDateTime);
}
