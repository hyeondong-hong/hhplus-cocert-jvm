package io.hhplus.concert.apps.integration.user.usecase;

import io.hhplus.concert.app.user.domain.ServiceEntry;
import io.hhplus.concert.app.user.domain.Token;
import io.hhplus.concert.app.user.port.ServiceEntryPort;
import io.hhplus.concert.app.user.port.TokenPort;
import io.hhplus.concert.app.user.usecase.QueueEjectUseCase;
import io.hhplus.concert.config.IntegrationTestService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
public class QueueEjectUseCaseIntegrationTest {

    @Autowired
    private IntegrationTestService integrationTestService;

    @Autowired
    private QueueEjectUseCase queueEjectUseCase;

    @Autowired
    private ServiceEntryPort serviceEntryPort;

    @Autowired
    private TokenPort tokenPort;

    @AfterEach
    public void tearDown() {
        integrationTestService.tearDown();
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
