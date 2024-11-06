package io.hhplus.concert.app.concert.port.jpa;

import io.hhplus.concert.app.concert.domain.ConcertSeat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConcertSeatJpaRepository extends JpaRepository<ConcertSeat, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ConcertSeat c WHERE c.id = :id")
    Optional<ConcertSeat> findByIdWithLock(Long id);

    List<ConcertSeat> findAllByIdIn(List<Long> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ConcertSeat c WHERE c.id in :ids")
    List<ConcertSeat> findAllByIdInWithLock(List<Long> ids);

    List<ConcertSeat> findAllByConcertScheduleId(Long concertScheduleId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ConcertSeat c WHERE c.concertScheduleId = :concertScheduleId")
    List<ConcertSeat> findAllByConcertScheduleIdWithLock(Long concertScheduleId);
}
