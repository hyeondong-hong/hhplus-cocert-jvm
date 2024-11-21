package io.hhplus.concert.app.kafka.adapter.consumer;

import io.hhplus.concert.app.kafka.domain.event.KafkaSampleEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Slf4j
@Service
public class KafkaSampleConsumer {

    @KafkaListener(topics = "sample-topic", groupId = "sample-group")
    public void consume(KafkaSampleEvent event) {
        log.info("Consumed event({}) for sample-topic: {}", event.sampleName(), event.someMessage());
    }
}
