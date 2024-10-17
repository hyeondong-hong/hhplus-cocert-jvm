package io.hhplus.concert.integration.user.usecase;

import io.hhplus.concert.user.domain.ServiceEntry;
import io.hhplus.concert.user.domain.Token;
import io.hhplus.concert.user.port.ServiceEntryPort;
import io.hhplus.concert.user.port.TokenPort;
import io.hhplus.concert.user.usecase.QueueEjectUseCase;
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
    private QueueEjectUseCase queueEjectUseCase;

    @Autowired
    private ServiceEntryPort serviceEntryPort;

    @Autowired
    private TokenPort tokenPort;

    @BeforeEach
    public void setUp() {
        for (int i = 0; i < 10; i++) {
            Token t = new Token();
            t.setUserId(i + 10L);
            t.setExpiresAt(LocalDateTime.now().plusDays(1));
            t.setCreatedAt(LocalDateTime.now().minusMinutes(15));
            tokenPort.save(t);
        }
        for (int i = 0; i < 10; i++) {
            ServiceEntry se = new ServiceEntry();
            se.setTokenId(i + 1L);
            se.setEntryAt(LocalDateTime.now().minusMinutes(12));
            se.setEnrolledAt(LocalDateTime.now().minusMinutes(10));
            serviceEntryPort.save(se);
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
