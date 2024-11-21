package io.hhplus.concert.app.kafka.port.producer;

import io.hhplus.concert.app.kafka.domain.event.KafkaSampleEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Slf4j
@Service
public class KafkaSampleProducer {

    private final KafkaTemplate<String, KafkaSampleEvent> kafkaTemplate;

    public void publish(KafkaSampleEvent event) {
        kafkaTemplate.send("sample-topic", event.sampleName(), event);
        log.info("Published event({}) for sample-topic: {}", event.sampleName(), event.someMessage());
    }
}
