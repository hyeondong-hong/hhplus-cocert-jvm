package io.hhplus.concert.apps.integration.user.usecase;

import io.hhplus.concert.app.user.domain.ServiceEntry;
import io.hhplus.concert.app.user.port.ServiceEntryPort;
import io.hhplus.concert.app.user.usecase.QueueEnrollUseCase;
import io.hhplus.concert.config.IntegrationTestService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("test")
public class QueueEnrollUseCaseIntegrationTest {

    @Autowired
    private IntegrationTestService integrationTestService;

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
        integrationTestService.tearDown();
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
