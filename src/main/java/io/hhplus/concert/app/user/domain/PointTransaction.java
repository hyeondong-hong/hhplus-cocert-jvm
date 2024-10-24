package io.hhplus.concert.app.user.domain;

import io.hhplus.concert.app.user.domain.enm.PointTransactionStatus;
import io.hhplus.concert.app.user.domain.enm.PointTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@Entity
@Table(
        indexes = {
                @Index(columnList = "user_point_id"),
                @Index(columnList = "payment_id"),
        }
)
@NoArgsConstructor
@AllArgsConstructor
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userPointId;

    @Column
    private Integer remains;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointTransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointTransactionStatus status;

    @Column
    private Long paymentId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime modifiedAt;

    public void setCompleted(Integer remains) {
        this.remains = remains;
        this.status = PointTransactionStatus.COMPLETE;
    }

    public void setCancelled() {
        this.status = PointTransactionStatus.CANCELLED;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (modifiedAt == null) {
            modifiedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (modifiedAt == null) {
            modifiedAt = LocalDateTime.now();
        }
    }
}
