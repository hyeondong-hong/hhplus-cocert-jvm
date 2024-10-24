package io.hhplus.concert.app.concert.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@Table(
        indexes = {
                @Index(columnList = "concert_schedule_id")
        }
)
@NoArgsConstructor
@AllArgsConstructor
public class ConcertSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long concertScheduleId;

    @Column
    private String label;

    @Column(nullable = false)
    private Integer seatNumber;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column
    private Boolean isActive;

    public void open() {
        this.isActive = true;
    }

    public void close() {
        this.isActive = false;
    }

    public boolean isClosed() {
        return !this.isActive;
    }

    @PrePersist
    protected void onCreate() {
        if (isActive == null) {
            isActive = true;
        }
    }
}
