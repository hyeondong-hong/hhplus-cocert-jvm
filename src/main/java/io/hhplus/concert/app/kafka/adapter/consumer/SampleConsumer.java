package io.hhplus.concert.app.kafka.adapter.consumer;

import io.hhplus.concert.app.kafka.domain.event.SampleEvent;
import io.hhplus.concert.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class SampleConsumer {

    public static final String TOPIC = "sample-topic";

    @KafkaListener(topics = TOPIC, groupId = KafkaConfig.KAFKA_GROUP_ID)
    public void consume(SampleEvent event) {
        log.info("Consumed event({}) for {}: {}", event.sampleName(), TOPIC, event.someMessage());
    }
}
