package io.hhplus.concert.unit.user.usecase;

import io.hhplus.concert.user.domain.UserPoint;
import io.hhplus.concert.user.port.UserPointPort;
import io.hhplus.concert.user.port.UserPort;
import io.hhplus.concert.user.usecase.RetrieveUserPointUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RetrieveUserPointUseCaseUnitTest {

    @Mock
    private UserPointPort userPointPort;

    @Mock
    private UserPort userPort;

    @InjectMocks
    private RetrieveUserPointUseCase retrieveUserPointUseCase;

    private UserPoint userPoint;

    @BeforeEach
    public void setUp() {
        userPoint = new UserPoint();
        userPoint.setId(1L);
        userPoint.setUserId(1L);
        userPoint.setRemains(5000);
    }

    @Test
    @DisplayName("유저가 없을 경우 예외가 발생한다")
    public void userNotFound() {
        when(userPort.isExists(eq(userPoint.getUserId()))).thenReturn(false);
        NoSuchElementException e = new NoSuchElementException("User not found: userId = " + userPoint.getUserId());
        NoSuchElementException ex = assertThrows(
                NoSuchElementException.class,
                () -> retrieveUserPointUseCase.execute(
                        new RetrieveUserPointUseCase.Input(
                                userPoint.getUserId()
                        )
                )
        );
        assertEquals(e.getMessage(), ex.getMessage());
    }

    @Test
    @DisplayName("유저 포인트 정보를 조회한다")
    public void retrieveUserPoint() {
        when(userPort.isExists(eq(userPoint.getUserId()))).thenReturn(true);
        when(userPointPort.getByUserId(eq(userPoint.getUserId()))).thenReturn(userPoint);

        RetrieveUserPointUseCase.Output output = retrieveUserPointUseCase.execute(
                new RetrieveUserPointUseCase.Input(
                        userPoint.getUserId()
                )
        );

        assertEquals(userPoint.getUserId(), output.userPointResult().userId());
        assertEquals(userPoint.getRemains(), output.userPointResult().remains());
    }
}
