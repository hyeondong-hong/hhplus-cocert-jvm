package io.hhplus.concert.concert.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(
        indexes = {
                @Index(columnList = "concert_schedule_id")
        }
)
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
    private Boolean isActive = true;
}
