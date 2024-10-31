package io.hhplus.concert.apps.integration.concert.concurrent;

import io.hhplus.concert.app.user.domain.Token;
import io.hhplus.concert.app.user.port.TokenPort;
import io.hhplus.concert.config.IntegrationTestService;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ReservationConcurrentIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private IntegrationTestService integrationTestService;

    @Autowired
    private TokenPort tokenPort;

    @BeforeAll
    public static void setUpAll() {
        RestAssured.baseURI = "http://localhost";
    }

    @BeforeEach
    public void setUp() {
        integrationTestService.enrolledSetUp();
    }

    @AfterEach
    public void tearDown() {
        integrationTestService.tearDown();
    }

    @Test
    @DisplayName("유저 예약 동시성 테스트")
    public void reservationConcurrent() throws Exception {
        List<Token> tokens = tokenPort.findAll();
        Long concertId = 1L;
        Long concertScheduleId = 1L;

        CountDownLatch standbyLatch = new CountDownLatch(1);
        CountDownLatch latch = new CountDownLatch(tokens.size());
        Map<Long, Response> responseMap = new HashMap<>();
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            Long concertSeatId = (i % 5L) + 1L;  // 1,2,3,4,5번 좌석으로만 경쟁
            CompletableFuture.runAsync(() -> {
                try {
                    standbyLatch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                responseMap.put(token.getId(), reservation(token.getKeyUuid(), concertId, concertScheduleId, concertSeatId));
                latch.countDown();
            });
        }
        standbyLatch.countDown();
        latch.await();

        assertEquals(
                5,  // 5개 요청만 예약에 성공해야 함.
                responseMap.values().stream().filter(response -> response.getStatusCode() == 201).count()
        );
    }

    private Response reservation(String uuid, Long concertId, Long concertScheduleId, Long concertSeatId) {

        return given()
                .port(port).accept(MediaType.APPLICATION_JSON_VALUE).headers("Authorization", uuid)
                .when().post(
                        "/api/1.0/concerts/{concertId}/schedules/{concertScheduleId}/seats/{concertSeatId}/reservations/pending",
                        concertId, concertScheduleId, concertSeatId
                )
                .thenReturn();
    }
}
