package io.hhplus.concert.config.infra.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class KafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(String topic, Object data) {
        kafkaTemplate.send(topic, data);
        log.info("Published event for {}: {}", topic, data.toString());
    }

    /*
    * 키를 해싱해서 특정한 파티션으로 할당하는 방법.
    * 같은 파티션 끼리 순서를 보장하도록 할 수 있지만 부하 분산이 문제가 될 수 있음.
    */
    public void publish(String topic, String key, Object data) {
        kafkaTemplate.send(topic, key, data);
        log.info("Published event({}) for {}: {}", key, topic, data.toString());
    }
}
