package io.hhplus.concert.app.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class UserPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private Integer remains;

    public void add(Integer amount) {
        this.remains += amount;
    }

    public void add(BigDecimal amount) {
        add(amount.intValue());
    }

    public boolean isEnough(Integer amount) {
        return this.remains >= amount;
    }

    public boolean isEnough(BigDecimal amount) {
        return BigDecimal.valueOf(this.remains).compareTo(amount) >= 0;
    }

    public void deduct(Integer amount) {
        assert this.remains >= amount;
        this.remains -= amount;
    }

    public void deduct(BigDecimal amount) {
        deduct(amount.intValue());
    }

    @PrePersist
    protected void onCreate() {
        if (remains == null) {
            remains = 0;
        }
    }
}
