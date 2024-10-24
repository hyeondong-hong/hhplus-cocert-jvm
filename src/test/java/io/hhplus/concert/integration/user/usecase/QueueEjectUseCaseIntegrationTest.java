package io.hhplus.concert.integration.user.usecase;

import io.hhplus.concert.app.user.domain.ServiceEntry;
import io.hhplus.concert.app.user.domain.Token;
import io.hhplus.concert.app.user.port.ServiceEntryPort;
import io.hhplus.concert.app.user.port.TokenPort;
import io.hhplus.concert.app.user.usecase.QueueEjectUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
public class QueueEjectUseCaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private QueueEjectUseCase queueEjectUseCase;

    @Autowired
    private ServiceEntryPort serviceEntryPort;

    @Autowired
    private TokenPort tokenPort;

    @AfterEach
    public void tearDown() {
        jdbcTemplate.execute("truncate table hhplus_concert_test.concert;");
        jdbcTemplate.execute("truncate table hhplus_concert_test.concert_schedule;");
        jdbcTemplate.execute("truncate table hhplus_concert_test.concert_seat;");
        jdbcTemplate.execute("truncate table hhplus_concert_test.payment;");
        jdbcTemplate.execute("truncate table hhplus_concert_test.payment_transaction;");
        jdbcTemplate.execute("truncate table hhplus_concert_test.point_transaction;");
        jdbcTemplate.execute("truncate table hhplus_concert_test.reservation;");
        jdbcTemplate.execute("truncate table hhplus_concert_test.user;");
        jdbcTemplate.execute("truncate table hhplus_concert_test.user_point;");
        jdbcTemplate.execute("truncate table hhplus_concert_test.token;");
        jdbcTemplate.execute("truncate table hhplus_concert_test.service_entry;");
    }

    @BeforeEach
    public void setUp() {
        for (int i = 0; i < 10; i++) {
            tokenPort.save(
                    Token.builder()
                            .userId(i + 10L)
                            .expiresAt(LocalDateTime.now().plusDays(1))
                            .createdAt(LocalDateTime.now().minusMinutes(15))
                            .build()
            );
        }
        for (int i = 0; i < 10; i++) {
            serviceEntryPort.save(
                    ServiceEntry.builder()
                            .tokenId(i + 1L)
                            .entryAt(LocalDateTime.now().minusMinutes(12))
                            .enrolledAt(LocalDateTime.now().minusMinutes(10))
                            .build()
            );
        }
    }

    @Test
    @DisplayName("등록 후 10분이 지난 이용자를 방출한다")
    public void ejectTokens() {
        queueEjectUseCase.execute(new QueueEjectUseCase.Input());
        NoSuchElementException e = assertThrows(
                NoSuchElementException.class,
                () -> serviceEntryPort.getByTokenId(1L)
        );
        assertEquals("No value present", e.getMessage());
    }

}
