package io.hhplus.concert.apps.integration.kafka.infra;

import io.hhplus.concert.app.kafka.adapter.consumer.KafkaSampleConsumer;
import io.hhplus.concert.app.kafka.domain.event.KafkaSampleEvent;
import io.hhplus.concert.app.kafka.port.producer.KafkaSampleProducer;
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
    private KafkaSampleProducer kafkaSampleProducer;

    @SpyBean
    private KafkaSampleConsumer kafkaSampleConsumer;

    @Test
    @DisplayName("카프카를 통해 이벤트를 발행하고 소비한다")
    public void test_Kafka() {
        kafkaSampleProducer.publish(new KafkaSampleEvent("event-name", "Hello, Kafka!"));
        verify(kafkaSampleConsumer, timeout(5000L)).consume(any(KafkaSampleEvent.class));
    }

}
