package io.hhplus.concert.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    @Value("${redisson.address}")
    private String address;

    @Value("${redisson.password}")
    private String password;

    @Value("${redisson.timeout}")
    private Integer timeout;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        SingleServerConfig ssc = config.useSingleServer();
        ssc.setAddress(address);
        if (password != null && !password.isEmpty()) {
            ssc.setPassword(password);
        }
        if (timeout != null && timeout > 0) {
            ssc.setTimeout(timeout);
        }

        return Redisson.create(config);
    }
}
