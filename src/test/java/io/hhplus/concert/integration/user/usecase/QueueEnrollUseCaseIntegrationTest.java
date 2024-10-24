package io.hhplus.concert.integration.user.usecase;

import io.hhplus.concert.app.user.domain.ServiceEntry;
import io.hhplus.concert.app.user.port.ServiceEntryPort;
import io.hhplus.concert.app.user.usecase.QueueEnrollUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("test")
public class QueueEnrollUseCaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private QueueEnrollUseCase queueEnrollUseCase;

    @Autowired
    private ServiceEntryPort serviceEntryPort;

    @BeforeEach
    public void setUp() {
        for (int i = 0; i < 31; i++) {
            serviceEntryPort.save(
                    ServiceEntry.builder()
                            .tokenId(i + 1L)
                            .entryAt(LocalDateTime.now())
                            .build()
            );
        }
    }

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

    @Test
    public void enrollTokens() {
        queueEnrollUseCase.execute(new QueueEnrollUseCase.Input());
        ServiceEntry enrolled = serviceEntryPort.getByTokenId(1L);
        ServiceEntry notYet = serviceEntryPort.getByTokenId(31L);

        assertNotNull(enrolled.getEnrolledAt());
        assertNull(notYet.getEnrolledAt());
    }
}
