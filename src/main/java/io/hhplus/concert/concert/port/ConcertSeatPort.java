package io.hhplus.concert.concert.port;

import io.hhplus.concert.concert.domain.ConcertSeat;
import io.hhplus.concert.concert.port.jpa.ConcertSeatJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@Repository
public class ConcertSeatPort {

    private final ConcertSeatJpaRepository jpaRepository;

    public Boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    public ConcertSeat get(Long id) {
        return jpaRepository.findById(id).orElseThrow();
    }

    public ConcertSeat getWithLock(Long id) {
        return jpaRepository.findByIdWithLock(id).orElseThrow();
    }

    public ConcertSeat save(ConcertSeat concertSeat) {
        return jpaRepository.save(concertSeat);
    }

    public List<ConcertSeat> saveAll(Collection<ConcertSeat> concertSeats) {
        return jpaRepository.saveAll(concertSeats);
    }

    public List<ConcertSeat> getAllByIdsWithLock(List<Long> ids) {
        return jpaRepository.findAllByIdIn(ids);
    }

    public List<ConcertSeat> getAllByConcertScheduleIdWithLock(Long concertScheduleId) {
        return jpaRepository.findAllByConcertScheduleId(concertScheduleId);
    }

    public List<Long> getAllIdsByConcertScheduleId(Long concertScheduleId) {
        return jpaRepository.findAllIdsByConcertScheduleId(concertScheduleId);
    }

    public List<Long> getAllIdsByConcertScheduleIdIn(List<Long> concertScheduleIds) {
        return jpaRepository.findAllIdsByConcertScheduleIdIn(concertScheduleIds);
    }
}
