package io.hhplus.concert.apps.unit.user.usecase;

import io.hhplus.concert.app.user.port.ServiceEntryPort;
import io.hhplus.concert.app.user.usecase.QueueEnrollUseCase;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class QueueEnrollUseCaseUnitTest {

    @Mock
    private ServiceEntryPort serviceEntryPort;

    @InjectMocks
    private QueueEnrollUseCase queueEnrollUseCase;

    List<Long> enrolled = new ArrayList<>();

    @AfterEach
    public void tearDown() {
        enrolled.clear();
    }

    // 좀 뻔한 테스트인데... 생략해도 되는지를 모르겠어서 일단은 추가
    @Test
    @DisplayName("대기열에 등록된 이용자 중 가장 빠른 30명을 서비스에 진입시킨다")
    public void enrollTokens() {
        when(serviceEntryPort.findEnrollableAllTokenIdByTop30WithLock()).thenReturn(List.of(11L,12L,13L,14L,15L,16L));
        doAnswer(i -> {
            enrolled.addAll(i.getArgument(0));
            return null;
        }).when(serviceEntryPort).enrollAllByTokenIds(any(List.class));

        queueEnrollUseCase.execute(new QueueEnrollUseCase.Input());

        Long[] expected = {11L,12L,13L,14L,15L,16L};
        assertArrayEquals(expected, enrolled.toArray());
    }
}
