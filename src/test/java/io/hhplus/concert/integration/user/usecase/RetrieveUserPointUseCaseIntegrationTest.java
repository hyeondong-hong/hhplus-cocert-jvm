package io.hhplus.concert.integration.user.usecase;

import io.hhplus.concert.app.user.domain.User;
import io.hhplus.concert.app.user.domain.UserPoint;
import io.hhplus.concert.app.user.port.UserPointPort;
import io.hhplus.concert.app.user.port.UserPort;
import io.hhplus.concert.app.user.usecase.RetrieveUserPointUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class RetrieveUserPointUseCaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RetrieveUserPointUseCase retrieveUserPointUseCase;

    @Autowired
    private UserPointPort userPointPort;

    @Autowired
    private UserPort userPort;

    private User user;
    private UserPoint userPoint;

    @BeforeEach
    public void setUp() {
        user = userPort.save(
                User.builder()
                        .username("테스트 유저")
                        .password("<PASSWORD>")
                        .createdAt(LocalDateTime.now())
                        .build()
        );
        userPoint = userPointPort.save(
                UserPoint.builder()
                        .userId(user.getId())
                        .remains(5000)
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
    @DisplayName("유저 ID로 해당 유저의 포인트를 조회한다")
    public void retrieveUserPoint() {
        RetrieveUserPointUseCase.Output output = retrieveUserPointUseCase.execute(
                new RetrieveUserPointUseCase.Input(
                        user.getId()
                )
        );

        assertEquals(output.userPointResult().remains(), userPoint.getRemains());
    }
}
