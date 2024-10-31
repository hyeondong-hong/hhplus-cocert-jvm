package io.hhplus.concert.apps.unit.concert.usecase;

import io.hhplus.concert.app.concert.domain.ConcertSeat;
import io.hhplus.concert.app.concert.port.ConcertPort;
import io.hhplus.concert.app.concert.port.ConcertSchedulePort;
import io.hhplus.concert.app.concert.port.ConcertSeatPort;
import io.hhplus.concert.app.concert.port.ReservationPort;
import io.hhplus.concert.app.concert.usecase.SearchAvailableSeatsUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SearchAvailableSeatsUseCaseUnitTest {

    @Mock
    private ConcertPort concertPort;

    @Mock
    private ConcertSchedulePort concertSchedulePort;

    @Mock
    private ConcertSeatPort concertSeatPort;

    @Mock
    private ReservationPort reservationPort;

    @InjectMocks
    private SearchAvailableSeatsUseCase searchAvailableSeatsUseCase;

    private List<ConcertSeat> selectedSeats = new ArrayList<>();

    @AfterEach
    public void tearDown() {
        selectedSeats.clear();
    }

    @Test
    @DisplayName("콘서트가 없으면 예외가 발생한다")
    public void noConcerts() {
        doThrow(new NoSuchElementException("Concert not found: " + 1L)).when(concertPort).existsOrThrow(any(Long.class));

        NoSuchElementException e = assertThrows(
                NoSuchElementException.class, () -> searchAvailableSeatsUseCase.execute(
                        new SearchAvailableSeatsUseCase.Input(
                                1L,
                                2L
                        )
                ));

        assertEquals("Concert not found: " + 1L, e.getMessage());
    }

    @Test
    @DisplayName("스케줄이 없으면 예외가 발생한다")
    public void noSchedules() {
        doThrow(new NoSuchElementException("Concert Schedule not found: " + 2L)).when(concertSchedulePort).existsOrThrow(any(Long.class));

        NoSuchElementException e = assertThrows(
                NoSuchElementException.class, () -> searchAvailableSeatsUseCase.execute(
                        new SearchAvailableSeatsUseCase.Input(
                                1L,
                                2L
                        )
                ));

        assertEquals("Concert Schedule not found: " + 2L, e.getMessage());
    }

    @Test
    @DisplayName("유효한 콘서트 좌석을 조회한다")
    public void searchSchedules() {

        when(concertSeatPort.getAllByConcertScheduleId(eq(2L))).then(r -> {
            List<ConcertSeat> items = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                ConcertSeat item = ConcertSeat.builder()
                        .id(i + 51L)
                        .concertScheduleId(r.getArgument(0))
                        .label("일반석")
                        .price(BigDecimal.valueOf(12000))
                        .seatNumber(i)
                        .build();
                if (i < 20) {
                    item.close();
                }
                items.add(item);
            }
            selectedSeats.addAll(items);
            return items;
        });

        SearchAvailableSeatsUseCase.Output output = searchAvailableSeatsUseCase.execute(
                new SearchAvailableSeatsUseCase.Input(
                        1L,
                        2L
                ));

        // 20건을 비활성화 했으니 index 19 까지는 비활성화 상태여야 함
        assertEquals(false, output.seatResults().get(19).isActive());
    }
}
