package io.hhplus.concert.app.user.port.jpa;

import io.hhplus.concert.app.user.domain.ServiceEntry;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceEntryJpaRepository extends JpaRepository<ServiceEntry, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e.tokenId FROM ServiceEntry e WHERE e.enrolledAt < :baseDateTime")
    List<Long> findAllTokenIdByEnrolledAtLessThan(LocalDateTime baseDateTime);

    void deleteByTokenId(Long tokenId);
    void deleteAllByTokenIdIn(Collection<Long> tokenIds);

    Optional<ServiceEntry> findByTokenId(Long tokenId);
    Boolean existsByTokenId(Long tokenId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e.tokenId FROM ServiceEntry e WHERE e.enrolledAt IS NULL ORDER BY e.entryAt ASC")
    List<Long> findAllTokenIdByOrderByEntryAtTopWithLock(Pageable pageable);

    @Query(value = """
        SELECT row_num FROM (
            SELECT token_id, ROW_NUMBER() OVER (ORDER BY entry_at) AS row_num
            FROM service_entry
            WHERE enrolled_at IS NULL
        ) AS ranked
        WHERE ranked.token_id = :tokenId
    """, nativeQuery = true)
    Long getEntryRankByTokenId(@Param("tokenId") Long tokenId);

    @Modifying
    @Query("UPDATE ServiceEntry e SET e.enrolledAt = :enrolledAt WHERE e.tokenId IN :tokenIds")
    Integer updateAllByTokenIdIn(
            Collection<Long> tokenIds,
            LocalDateTime enrolledAt
    );
}
