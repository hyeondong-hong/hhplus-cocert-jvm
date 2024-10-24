package io.hhplus.concert.integration.user.usecase;

import io.hhplus.concert.app.user.domain.Token;
import io.hhplus.concert.app.user.port.TokenPort;
import io.hhplus.concert.app.user.port.UserPointPort;
import io.hhplus.concert.app.user.port.UserPort;
import io.hhplus.concert.app.user.usecase.IssueTokenUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class IssueTokenUseCaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private IssueTokenUseCase issueTokenUseCase;

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

    @Test
    @DisplayName("유저 id로 조회 시 토큰이 발행된다")
    public void issueToken() {
        IssueTokenUseCase.Output output = issueTokenUseCase.execute(
                new IssueTokenUseCase.Input(
                        1L
                )
        );
        String uuid = output.tokenResult().keyUuid();
        Token token = tokenPort.getByKey(uuid);
        assertEquals(1L, token.getUserId());
    }
}
