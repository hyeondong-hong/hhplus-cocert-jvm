package io.hhplus.concert.app.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@Entity
@Table(
        indexes = {
                @Index(columnList = "entry_at"),
                @Index(columnList = "enrolled_at")
        }
)
@NoArgsConstructor
@AllArgsConstructor
public class ServiceEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long tokenId;

    @Column(nullable = false)
    private LocalDateTime entryAt;

    @Column
    private LocalDateTime enrolledAt;

    public boolean isEnrolled() {
        return enrolledAt != null;
    }
}
