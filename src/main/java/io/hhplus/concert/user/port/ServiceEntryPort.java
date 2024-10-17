package io.hhplus.concert.user.port;

import io.hhplus.concert.user.domain.ServiceEntry;
import io.hhplus.concert.user.port.jpa.ServiceEntryJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@Repository
public class ServiceEntryPort {

    private ServiceEntryJpaRepository jpaRepository;

    public ServiceEntry getByTokenId(Long tokenId) {
        return jpaRepository.findByTokenId(tokenId).orElseThrow();
    }

    public Boolean exists(Long tokenId) {
        return jpaRepository.existsByTokenId(tokenId);
    }

    public ServiceEntry save(ServiceEntry serviceEntry) {
        return jpaRepository.save(serviceEntry);
    }

    public Long getEntryRankByTokenId(Long tokenId) {
        return jpaRepository.getEntryRankByTokenId(tokenId);
    }

    public List<Long> findEnrollableAllTokenIdByTop30WithLock() {
        return jpaRepository.findAllTokenIdByOrderByEntryAtTopWithLock(PageRequest.of(0, 30));
    }

    public void enrollAllByTokenIds(Collection<Long> tokenIds) {
        jpaRepository.updateAllByTokenIdIn(tokenIds, LocalDateTime.now());
    }

    public List<Long> findAllTokenIdExpiredWithLock() {
        return jpaRepository.findAllTokenIdByEnrolledAtLessThan(LocalDateTime.now().minusMinutes(10L));
    }

    public void delete(Long tokenId) {
        jpaRepository.deleteByTokenId(tokenId);
    }

    public void deleteAllByTokenIds(Collection<Long> tokenIds) {
        jpaRepository.deleteAllByTokenIdIn(tokenIds);
    }
}
