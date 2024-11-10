package io.hhplus.concert.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private Integer port;

    @Value("${redis.url}")
    private String url;

    @Value("${redis.password}")
    private String password;

    @Value("${redis.timeout}")
    private Integer timeout;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        SingleServerConfig ssc = config.useSingleServer();
        ssc.setAddress(url);
        if (password != null && !password.isEmpty()) {
            ssc.setPassword(password);
        }
        if (timeout != null && timeout > 0) {
            ssc.setTimeout(timeout);
        }

        return Redisson.create(config);
    }

    @Bean
    public RedisTemplate<String, ?> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, ?> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig())
                .build();
    }
}
