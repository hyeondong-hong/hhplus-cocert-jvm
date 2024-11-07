package io.hhplus.concert.app.concert.port;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.concert.app.concert.domain.Concert;
import io.hhplus.concert.app.concert.domain.ConcertSchedule;
import io.hhplus.concert.app.concert.port.cache.CacheRepository;
import io.hhplus.concert.config.dto.SerializablePageImpl;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@Repository
public class ConcertItemsRedisPort implements CacheRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String CACHE_PREFIX = "cache:concert:items:";

    @Override
    public Long clearCache() {
        Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
        if (keys == null) {
            return 0L;
        }
        return redisTemplate.delete(keys);
    }

    @Override
    public <T> T get(
            String cacheKey,
            Class<T> cls
    ) {
        return cls.cast(redisTemplate.opsForValue().get(cacheKey));
    }

    @Override
    public <T> Iterable<T> getList(String cacheKey, Class<T> cls) {
        Object value = redisTemplate.opsForValue().get(cacheKey);
        if (value == null) {
            return null;
        }

        String jsonValue = (String) value;
        try {
                return objectMapper.readValue(
                        jsonValue,
                        objectMapper.getTypeFactory().constructParametricType(
                                SerializablePageImpl.class,
                                cls
                        )
                );

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize cache data: " + jsonValue, e);
        }
    }

    @Override
    public void set(
            String cacheKey,
            Object value
    ) {
        try {
            @SuppressWarnings({"rawtypes", "unchecked"})
            String jsonValue = objectMapper.writeValueAsString(new SerializablePageImpl<>((Page) value));
            redisTemplate.opsForValue().set(cacheKey, jsonValue, 24, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    private String generateCacheKey(
            @NonNull Pageable pageable
    ) {
        return generateCacheKey(null, pageable);
    }

    @NonNull
    private String generateCacheKey(
            @Nullable Long concertId,
            @NonNull Pageable pageable
    ) {
        String key = "N=" + pageable.getPageNumber() + "&C=" + pageable.getPageSize() + "&S=" + pageable.getSort();
        if (concertId != null) {
            key = "concertId=" + concertId + "$" + key;
        }
        return CACHE_PREFIX + key;
    }

    @Nullable
    public Page<Concert> getConcerts(
            @NonNull Pageable pageable
    ) {
        String cacheKey = generateCacheKey(pageable);
        return (Page<Concert>) getList(cacheKey, Concert.class);
    }

    public void setConcerts(
            @NonNull Pageable pageable,
            @NonNull Page<Concert> concerts
    ) {
        String cacheKey = generateCacheKey(pageable);
        set(cacheKey, concerts);
    }

    @Nullable
    public Page<ConcertSchedule> getConcertSchedules(
            @NonNull Long concertId,
            @NonNull Pageable pageable
    ) {
        String cacheKey = generateCacheKey(concertId, pageable);
        return (Page<ConcertSchedule>) getList(cacheKey, ConcertSchedule.class);
    }

    public void setConcertSchedules(
            @NonNull Long concertId,
            @NonNull Pageable pageable,
            @NonNull Page<ConcertSchedule> concertSchedules
    ) {
        String cacheKey = generateCacheKey(concertId, pageable);
        set(cacheKey, concertSchedules);
    }
}
