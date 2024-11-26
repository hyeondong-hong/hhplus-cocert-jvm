package io.hhplus.concert.app.concert.adapter.consumer;

import io.hhplus.concert.app.concert.domain.ConcertSeat;
import io.hhplus.concert.app.concert.domain.Reservation;
import io.hhplus.concert.app.concert.domain.enm.ReservationStatus;
import io.hhplus.concert.app.concert.domain.event.ReservationSeatEvent;
import io.hhplus.concert.app.concert.port.ConcertSeatPort;
import io.hhplus.concert.app.concert.port.ReservationPort;
import io.hhplus.concert.app.payment.domain.Payment;
import io.hhplus.concert.app.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.app.payment.port.PaymentPort;
import io.hhplus.concert.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Slf4j
@Component
public class ReservationSeatConsumer {
    public static final String TOPIC = "reservation-seat";

    private final ConcertSeatPort concertSeatPort;
    private final ReservationPort reservationPort;
    private final PaymentPort paymentPort;

    @Transactional
    @KafkaListener(topics = TOPIC, groupId = KafkaConfig.KAFKA_GROUP_ID)
    public void consume(ReservationSeatEvent event) {
        ConcertSeat seat = concertSeatPort.get(event.concertSeatId());
        if (seat.isClosed()) {
            log.info("예약된 좌석에 예약 시도: userId = {}, concertSeatId = {}", event.userId(), seat.getId());
            return;
        }

        Payment payment = paymentPort.save(
                Payment.builder()
                        .userId(event.userId())
                        .price(seat.getPrice())
                        .status(PaymentStatus.PENDING)
                        .dueAt(LocalDateTime.now().plusMinutes(5L))
                        .build()
        );

        reservationPort.save(
                Reservation.builder()
                        .userId(event.userId())
                        .concertSeatId(event.concertSeatId())
                        .status(ReservationStatus.PENDING)
                        .paymentId(payment.getId())
                        .build()
        );

        seat.close();
        concertSeatPort.save(seat);
    }
}
