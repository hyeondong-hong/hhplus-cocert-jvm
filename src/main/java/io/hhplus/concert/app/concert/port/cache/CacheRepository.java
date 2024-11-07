package io.hhplus.concert.app.concert.port.cache;

public interface CacheRepository {
    Long clearCache();
    <T> T get(String cacheKey, Class<T> cls);
    <T> Iterable<T> getList(String cacheKey, Class<T> cls);
    void set(String cacheKey, Object value);
}
