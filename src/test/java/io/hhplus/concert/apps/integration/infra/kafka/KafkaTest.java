package io.hhplus.concert.apps.integration.infra.kafka;

import io.hhplus.concert.app.kafka.adapter.consumer.SampleConsumer;
import io.hhplus.concert.app.kafka.domain.event.SampleEvent;
import io.hhplus.concert.config.infra.kafka.KafkaProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
public class KafkaTest {

    @Autowired
    private KafkaProducer kafkaProducer;

    @SpyBean
    private SampleConsumer sampleConsumer;

    @Test
    @DisplayName("카프카를 통해 이벤트를 발행하고 소비한다")
    public void test_Kafka() {
        kafkaProducer.publish(SampleConsumer.TOPIC, new SampleEvent("event-name", "Hello, Kafka!"));
        verify(sampleConsumer, timeout(5000L)).consume(any(SampleEvent.class));
    }

}
