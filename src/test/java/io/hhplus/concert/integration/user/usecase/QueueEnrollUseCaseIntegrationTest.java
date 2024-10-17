package io.hhplus.concert.integration.user.usecase;

import io.hhplus.concert.user.domain.ServiceEntry;
import io.hhplus.concert.user.port.ServiceEntryPort;
import io.hhplus.concert.user.usecase.QueueEnrollUseCase;
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
    private QueueEnrollUseCase queueEnrollUseCase;

    @Autowired
    private ServiceEntryPort serviceEntryPort;

    @BeforeEach
    public void setUp() {
        for (int i = 0; i < 31; i++) {
            ServiceEntry se = new ServiceEntry();
            se.setTokenId(i + 1L);
            se.setEntryAt(LocalDateTime.now());
            serviceEntryPort.save(se);
        }
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
