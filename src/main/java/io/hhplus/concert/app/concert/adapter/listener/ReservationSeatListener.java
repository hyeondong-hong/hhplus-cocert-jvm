package io.hhplus.concert.app.concert.adapter.listener;

import io.hhplus.concert.app.concert.adapter.consumer.ReservationSeatConsumer;
import io.hhplus.concert.app.concert.domain.event.ReservationSeatEvent;
import io.hhplus.concert.config.infra.kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class ReservationSeatListener {

    private final KafkaProducer kafkaProducer;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEvent(ReservationSeatEvent event) {
        kafkaProducer.publish(ReservationSeatConsumer.TOPIC, event);
    }
}
