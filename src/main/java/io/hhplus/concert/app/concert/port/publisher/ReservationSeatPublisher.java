package io.hhplus.concert.app.concert.port.publisher;

import io.hhplus.concert.app.concert.domain.event.ReservationSeatEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ReservationSeatPublisher {
    private final ApplicationEventPublisher publisher;

    public void publish(ReservationSeatEvent event) {
        publisher.publishEvent(event);
    }
}
