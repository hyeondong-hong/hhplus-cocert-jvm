package io.hhplus.concert.app.concert.port;

import io.hhplus.concert.app.concert.domain.Reservation;
import io.hhplus.concert.app.concert.domain.enm.ReservationStatus;
import io.hhplus.concert.app.concert.port.jpa.ReservationJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@AllArgsConstructor
@Repository
public class ReservationPort {

    private final ReservationJpaRepository jpaRepository;

    public Reservation get(Long id) {
        return jpaRepository.findById(id).orElseThrow();
    }

    public Reservation getWithLock(Long id) {
        return jpaRepository.findByIdWithLock(id).orElseThrow();
    }

    public Reservation save(Reservation reservation) {
        return jpaRepository.save(reservation);
    }

    public List<Reservation> saveAll(List<Reservation> reservations) {
        return jpaRepository.saveAll(reservations);
    }

    public List<Long> getAllConcertSeatIdsByConcertSeatIdsWithLock(List<Long> concertSeatIds, List<ReservationStatus> statuses) {
        return jpaRepository.findAllConcertSeatIdsByConcertSeatIdInAndStatusInWithLock(concertSeatIds, statuses);
    }

    public List<Reservation> getAllByStatusesWithLock(List<ReservationStatus> statuses) {
        return jpaRepository.findAllByStatusIn(statuses);
    }

    public List<Reservation> getAllByConcertSeatIdsAndStatusesWithLock(List<Long> concertSeatIds, List<ReservationStatus> statuses) {
        return jpaRepository.findAllByConcertSeatIdInAndStatusIn(concertSeatIds, statuses);
    }
}
