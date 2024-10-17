package io.hhplus.concert.integration.user.usecase;

import io.hhplus.concert.user.domain.ServiceEntry;
import io.hhplus.concert.user.domain.Token;
import io.hhplus.concert.user.port.ServiceEntryPort;
import io.hhplus.concert.user.port.TokenPort;
import io.hhplus.concert.user.usecase.CheckTokenEnrolledUseCase;
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
    private CheckTokenEnrolledUseCase checkTokenEnrolledUseCase;

    @Autowired
    private ServiceEntryPort serviceEntryPort;

    @Autowired
    private TokenPort tokenPort;

    private String uuid;

    @BeforeEach
    public void setUp() {
        uuid = UUID.randomUUID().toString();
        Token t = new Token();
        t.setUserId(1L);
        t.setKeyUuid(uuid);
        t.setExpiresAt(LocalDateTime.now().plusDays(1));
        t.setCreatedAt(LocalDateTime.now());
        tokenPort.save(t);
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
        ServiceEntry se = new ServiceEntry();
        se.setTokenId(t.getId());
        se.setEntryAt(LocalDateTime.now());
        se.setEnrolledAt(LocalDateTime.now());
        serviceEntryPort.save(se);

        CheckTokenEnrolledUseCase.Output output = checkTokenEnrolledUseCase.execute(new CheckTokenEnrolledUseCase.Input(uuid));

        assertEquals(true, output.isAuthenticated());
        assertNull(output.rank());
    }
}
