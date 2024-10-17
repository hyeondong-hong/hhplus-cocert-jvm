package io.hhplus.concert.concert.port.jpa;

import io.hhplus.concert.concert.domain.ConcertSeat;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConcertSeatJpaRepository extends JpaRepository<ConcertSeat, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ConcertSeat c WHERE c.id = :id")
    Optional<ConcertSeat> findByIdWithLock(Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<ConcertSeat> findAllByIdIn(List<Long> ids);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<ConcertSeat> findAllByConcertScheduleId(Long concertScheduleId);
    List<Long> findAllIdsByConcertScheduleId(Long concertScheduleId);
    List<Long> findAllIdsByConcertScheduleIdIn(Collection<Long> concertScheduleIds);
}
