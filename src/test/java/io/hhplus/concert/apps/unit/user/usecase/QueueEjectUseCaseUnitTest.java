package io.hhplus.concert.apps.unit.user.usecase;

import io.hhplus.concert.app.user.port.ServiceEntryPort;
import io.hhplus.concert.app.user.port.TokenPort;
import io.hhplus.concert.app.user.usecase.QueueEjectUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class QueueEjectUseCaseUnitTest {

    @Mock
    private ServiceEntryPort serviceEntryPort;

    @Mock
    private TokenPort tokenPort;

    @InjectMocks
    private QueueEjectUseCase queueEjectUseCase;

    List<Long> ejected = new ArrayList<>();

    @AfterEach
    public void tearDown() {
        ejected.clear();
    }

    @Test
    @DisplayName("이용자를 대기열에서 방출할 때 병합된 토큰 ID 배열에는 중복된 ID가 없어야 한다")
    public void ejectMergedTokens() {
        when(tokenPort.findAllIdsExpired()).thenReturn(List.of(1L,2L,3L,4L,5L,6L));
        when(serviceEntryPort.findAllTokenIdExpired()).thenReturn(List.of(3L,5L,7L,9L,11L));
        doAnswer(i -> {
            ejected.addAll(i.getArgument(0));
            return null;
        }).when(serviceEntryPort).deleteAllByTokenIds(any(List.class));

        queueEjectUseCase.execute(new QueueEjectUseCase.Input());

        Long[] expected = {1L,2L,3L,4L,5L,6L,7L,9L,11L};
        assertArrayEquals(expected, ejected.toArray());
    }
}
