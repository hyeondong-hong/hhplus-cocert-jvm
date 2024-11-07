package io.hhplus.concert.app.concert.domain;

import io.hhplus.concert.app.concert.domain.enm.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@Table(
        indexes = {
                @Index(columnList = "user_id,concert_seat_id")
        }
)
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long concertSeatId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(nullable = false)
    private Long paymentId;

    public void complete() {
        this.status = ReservationStatus.COMPLETE;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }
}
