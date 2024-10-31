package io.hhplus.concert.app.concert.port;

import io.hhplus.concert.app.concert.domain.ConcertSeat;
import io.hhplus.concert.app.concert.port.jpa.ConcertSeatJpaRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Repository
public class ConcertSeatPort {

    private final ConcertSeatJpaRepository jpaRepository;

    public Boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    public void existsOrThrow(Long id) {
        if (!existsById(id)) {
            log.warn("유효하지 않은 콘서트 좌석에 접근: concertSeatId = {}", id);
            throw new IllegalArgumentException("Concert Seat not found: " + id);
        }
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
}
