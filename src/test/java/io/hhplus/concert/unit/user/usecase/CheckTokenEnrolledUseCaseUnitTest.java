package io.hhplus.concert.unit.user.usecase;

import io.hhplus.concert.user.domain.ServiceEntry;
import io.hhplus.concert.user.domain.Token;
import io.hhplus.concert.user.port.ServiceEntryPort;
import io.hhplus.concert.user.port.TokenPort;
import io.hhplus.concert.user.usecase.CheckTokenEnrolledUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CheckTokenEnrolledUseCaseUnitTest {

    @Mock
    private ServiceEntryPort serviceEntryPort;

    @Mock
    private TokenPort tokenPort;

    @InjectMocks
    private CheckTokenEnrolledUseCase checkTokenEnrolledUseCase;

    private Token token;

    @BeforeEach
    public void setUp() {
        token = new Token();
        token.setId(1L);
        token.setUserId(11L);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusDays(1));
    }

    @Test
    @DisplayName("토큰이 존재하지 않으면 오류가 발생한다")
    public void tokenNotFound() {
        Throwable e = new BadCredentialsException("존재하지 않는 토큰: keyUuid = " + token.getKeyUuid());
        when(tokenPort.getByKey(eq(token.getKeyUuid()))).thenThrow(e);

        BadCredentialsException r = assertThrows(
                BadCredentialsException.class,
                () -> checkTokenEnrolledUseCase.execute(
                        new CheckTokenEnrolledUseCase.Input(token.getKeyUuid())
                )
        );
        assertEquals(e.getMessage(), r.getMessage());
    }

    @Test
    @DisplayName("토큰이 존재하면서 등록된 상태가 아니면 대기열에 등록한다")
    public void notEntriesToken() {
        when(tokenPort.getByKey(eq(token.getKeyUuid()))).then(r -> token);
        when(serviceEntryPort.exists(eq(token.getId()))).thenReturn(false);
        when(serviceEntryPort.save(any(ServiceEntry.class))).then(r -> {
            ServiceEntry serviceEntry = r.getArgument(0);
            serviceEntry.setId(111L);
            return serviceEntry;
        });
        when(serviceEntryPort.getEntryRankByTokenId(eq(token.getId()))).thenReturn(100L);

        CheckTokenEnrolledUseCase.Output output = checkTokenEnrolledUseCase.execute(
                new CheckTokenEnrolledUseCase.Input(token.getKeyUuid())
        );

        assertEquals(false, output.isAuthenticated());
        assertEquals(100L, output.rank());
    }

    @Test
    @DisplayName("토큰이 존재하면서 등록된 상태이고 미진입 상태면 현재 상태와 순서를 반환한다")
    public void entriesTokenAndWait() {
        when(tokenPort.getByKey(eq(token.getKeyUuid()))).then(r -> token);
        when(serviceEntryPort.exists(eq(token.getId()))).thenReturn(true);
        when(serviceEntryPort.getByTokenId(eq(token.getId()))).then(r -> {
            ServiceEntry serviceEntry = new ServiceEntry();
            serviceEntry.setId(112L);
            serviceEntry.setTokenId(token.getId());
            serviceEntry.setEntryAt(LocalDateTime.now().minusMinutes(2));
            serviceEntry.setEnrolledAt(null);
            return serviceEntry;
        });
        when(serviceEntryPort.getEntryRankByTokenId(eq(token.getId()))).thenReturn(20L);

        CheckTokenEnrolledUseCase.Output output = checkTokenEnrolledUseCase.execute(
                new CheckTokenEnrolledUseCase.Input(token.getKeyUuid())
        );

        assertEquals(false, output.isAuthenticated());
        assertEquals(20L, output.rank());
    }

    @Test
    @DisplayName("토큰이 존재하면서 등록된 상태이고 진입 상태면 현재 상태만 반환한다")
    public void entriesTokenAndEnrolled() {
        when(tokenPort.getByKey(eq(token.getKeyUuid()))).then(r -> token);
        when(serviceEntryPort.exists(eq(token.getId()))).thenReturn(true);
        when(serviceEntryPort.getByTokenId(eq(token.getId()))).then(r -> {
            ServiceEntry serviceEntry = new ServiceEntry();
            serviceEntry.setId(113L);
            serviceEntry.setTokenId(token.getId());
            serviceEntry.setEntryAt(LocalDateTime.now().minusMinutes(2));
            serviceEntry.setEnrolledAt(LocalDateTime.now().minusMinutes(1));
            return serviceEntry;
        });

        CheckTokenEnrolledUseCase.Output output = checkTokenEnrolledUseCase.execute(
                new CheckTokenEnrolledUseCase.Input(token.getKeyUuid())
        );

        assertEquals(true, output.isAuthenticated());
        assertNull(output.rank());
    }
}
