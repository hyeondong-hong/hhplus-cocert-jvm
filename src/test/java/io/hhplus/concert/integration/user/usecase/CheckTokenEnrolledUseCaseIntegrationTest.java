package io.hhplus.concert.integration.user.usecase;

import io.hhplus.concert.app.user.domain.ServiceEntry;
import io.hhplus.concert.app.user.domain.Token;
import io.hhplus.concert.app.user.port.ServiceEntryPort;
import io.hhplus.concert.app.user.port.TokenPort;
import io.hhplus.concert.app.user.usecase.CheckTokenEnrolledUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("test")
public class CheckTokenEnrolledUseCaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CheckTokenEnrolledUseCase checkTokenEnrolledUseCase;

    @Autowired
    private ServiceEntryPort serviceEntryPort;

    @Autowired
    private TokenPort tokenPort;

    private String uuid;

    @BeforeEach
    public void setUp() {
        tokenPort.save(
                Token.builder()
                        .userId(1L)
                        .keyUuid(uuid = UUID.randomUUID().toString())
                        .expiresAt(LocalDateTime.now().plusDays(1))
                        .createdAt(LocalDateTime.now())
                        .build()
        );
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
    @DisplayName("신규 등록된 토큰은 서비스 등록과 함께 미진입 및 대기 순번을 받는다")
    public void entryNewToken() {
        CheckTokenEnrolledUseCase.Output output = checkTokenEnrolledUseCase.execute(new CheckTokenEnrolledUseCase.Input(uuid));

        assertEquals(false, output.isAuthenticated());
        assertEquals(1, output.rank());
    }

    @Test
    @DisplayName("진입 완료된 토큰은 진입된 상태를 받는다")
    public void checkTokenEnrolled() {
        Token t = tokenPort.getByKey(uuid);
        serviceEntryPort.save(
                ServiceEntry.builder()
                        .tokenId(t.getId())
                        .entryAt(LocalDateTime.now())
                        .enrolledAt(LocalDateTime.now())
                        .build()
        );

        CheckTokenEnrolledUseCase.Output output = checkTokenEnrolledUseCase.execute(
                new CheckTokenEnrolledUseCase.Input(
                        uuid
                )
        );

        assertEquals(true, output.isAuthenticated());
        assertNull(output.rank());
    }
}
