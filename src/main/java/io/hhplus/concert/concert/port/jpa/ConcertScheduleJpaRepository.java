package io.hhplus.concert.concert.port.jpa;

import io.hhplus.concert.concert.domain.ConcertSchedule;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConcertScheduleJpaRepository extends JpaRepository<ConcertSchedule, Long> {

    Page<ConcertSchedule> findAllByConcertId(Long concertId, Pageable pageable);
    List<Long> findAllIdsByConcertIdAndScheduledAtGreaterThanEqual(Long concertId, LocalDateTime baseDateTime);
}
