package io.hhplus.concert.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        indexes = {
                @Index(columnList = "entry_at"),
                @Index(columnList = "enrolled_at")
        }
)
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
}
