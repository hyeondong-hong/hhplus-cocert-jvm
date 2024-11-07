package io.hhplus.concert.app.user.port;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@AllArgsConstructor
public class QueueRedisPort {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CACHE_PREFIX = "cache:user:queue:";

    private enum KeyType {
        ENTRY, ENROLL
    }

    public void entry(@NonNull String keyUuid) {
        redisTemplate.opsForZSet().add(CACHE_PREFIX + KeyType.ENTRY.name(), keyUuid, System.currentTimeMillis());
    }

    @Nullable
    public Long getEntryRank(@NonNull String keyUuid) {
        return redisTemplate.opsForZSet().rank(CACHE_PREFIX + KeyType.ENTRY.name(), keyUuid);
    }

    public void enrolls(Long limit) {
        Set<Object> entries = redisTemplate.opsForZSet().range(CACHE_PREFIX + KeyType.ENTRY.name(), 0, limit);
        if (entries == null || entries.isEmpty()) {
            return;
        }

        redisTemplate.opsForZSet().add(CACHE_PREFIX + KeyType.ENROLL.name(), entries.toArray(), System.currentTimeMillis());
        redisTemplate.opsForZSet().remove(CACHE_PREFIX + KeyType.ENTRY.name(), entries.toArray());
    }

    @NonNull
    public Boolean getEnrolled(@NonNull String keyUuid) {
        Long rank = redisTemplate.opsForZSet().rank(CACHE_PREFIX + KeyType.ENROLL.name(), keyUuid);
        return rank != null;
    }

    public void ejects() {
        long expirationThreshold = System.currentTimeMillis() - (10 * 60 * 1000);
        redisTemplate.opsForZSet().removeRangeByScore(CACHE_PREFIX + KeyType.ENROLL.name(), 0, expirationThreshold);
    }
}
