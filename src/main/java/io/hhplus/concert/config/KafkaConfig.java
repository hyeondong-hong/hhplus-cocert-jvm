package io.hhplus.concert.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
public class KafkaConfig {

    public static final String KAFKA_GROUP_ID = "hhplus-concert";
}
