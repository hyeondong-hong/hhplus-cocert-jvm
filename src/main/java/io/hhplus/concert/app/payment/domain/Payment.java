package io.hhplus.concert.app.payment.domain;

import io.hhplus.concert.app.payment.domain.enm.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Builder
@Table(
        indexes = {
                @Index(columnList = "user_id")
        }
)
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String paymentKey;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private LocalDateTime dueAt;

    @Column
    private LocalDateTime paidAt;

    public void setPending() {
        this.status = PaymentStatus.PENDING;
    }

    public void setPaid() {
        this.status = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public void setRefunded() {
        this.status = PaymentStatus.REFUNDED;
    }

    public void setCancelled() {
        this.status = PaymentStatus.CANCELLED;
    }

    public boolean isExpiredBy(@NonNull LocalDateTime now) {
        return dueAt.isBefore(now);
    }

    public void expiredThenThrowBy(@NonNull LocalDateTime now) {
        if (isExpiredBy(now)) {
            throw new IllegalArgumentException("결제 기한 만료");
        }
    }

    public boolean isExpired() {
        return isExpiredBy(LocalDateTime.now());
    }

    public void expiredThenThrow() {
        expiredThenThrowBy(LocalDateTime.now());
    }

    @PrePersist
    protected void onCreate() {
        if (paymentKey == null) {
            paymentKey = UUID.randomUUID().toString();
        }
    }
}
