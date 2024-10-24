package io.hhplus.concert.app.concert.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@Entity
@Table(
        indexes = {
                @Index(columnList = "concert_id")
        }
)
@NoArgsConstructor
@AllArgsConstructor
public class ConcertSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long concertId;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;
}
