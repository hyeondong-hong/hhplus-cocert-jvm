package io.hhplus.concert.app.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Entity
@Table(
        indexes = {
                @Index(columnList = "user_id")
        }
)
@NoArgsConstructor
@AllArgsConstructor
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String keyUuid;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public boolean isExpiredBy(@NonNull LocalDateTime now) {
        return expiresAt.isBefore(now);
    }

    public boolean isExpired() {
        return isExpiredBy(LocalDateTime.now());
    }

    @PrePersist
    protected void onCreate() {
        if (keyUuid == null) {
            keyUuid = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
