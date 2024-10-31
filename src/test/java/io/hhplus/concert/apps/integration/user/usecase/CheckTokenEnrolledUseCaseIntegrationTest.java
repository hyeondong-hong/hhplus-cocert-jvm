package io.hhplus.concert.apps.integration.user.usecase;

import io.hhplus.concert.app.user.domain.ServiceEntry;
import io.hhplus.concert.app.user.domain.Token;
import io.hhplus.concert.app.user.port.ServiceEntryPort;
import io.hhplus.concert.app.user.port.TokenPort;
import io.hhplus.concert.app.user.usecase.CheckTokenEnrolledUseCase;
import io.hhplus.concert.config.IntegrationTestService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("test")
public class CheckTokenEnrolledUseCaseIntegrationTest {

    @Autowired
    private IntegrationTestService integrationTestService;

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
        integrationTestService.tearDown();
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
