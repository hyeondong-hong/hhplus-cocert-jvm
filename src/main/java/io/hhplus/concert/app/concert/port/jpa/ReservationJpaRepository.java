package io.hhplus.concert.app.concert.port.jpa;

import io.hhplus.concert.app.concert.domain.Reservation;
import io.hhplus.concert.app.concert.domain.enm.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :id")
    Optional<Reservation> findByIdWithLock(Long id);

    @Query("SELECT r.concertSeatId FROM Reservation r WHERE r.concertSeatId in :concertSeatIds and r.status in :statuses")
    List<Long> findAllConcertSeatIdsByConcertSeatIdInAndStatusIn(Collection<Long> concertSeatIds, Collection<ReservationStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r.concertSeatId FROM Reservation r WHERE r.concertSeatId in :concertSeatIds and r.status in :statuses")
    List<Long> findAllConcertSeatIdsByConcertSeatIdInAndStatusInWithLock(Collection<Long> concertSeatIds, Collection<ReservationStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Reservation> findAllByStatusIn(Collection<ReservationStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Reservation> findAllByConcertSeatIdInAndStatusIn(Collection<Long> concertSeatIds, Collection<ReservationStatus> statuses);
}
