package io.hhplus.concert.unit.concert.usecase;

import io.hhplus.concert.concert.domain.ConcertSeat;
import io.hhplus.concert.concert.port.ConcertPort;
import io.hhplus.concert.concert.port.ConcertSchedulePort;
import io.hhplus.concert.concert.port.ConcertSeatPort;
import io.hhplus.concert.concert.port.ReservationPort;
import io.hhplus.concert.concert.usecase.SearchAvailableSeatsUseCase;
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
        when(concertPort.existsById(any(Long.class))).thenReturn(false);

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
        when(concertPort.existsById(any(Long.class))).thenReturn(true);
        when(concertSchedulePort.existsById(any(Long.class))).thenReturn(false);

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
        when(concertPort.existsById(any(Long.class))).thenReturn(true);
        when(concertSchedulePort.existsById(any(Long.class))).thenReturn(true);

        when(concertSeatPort.getAllByConcertScheduleIdWithLock(eq(2L))).then(r -> {
            List<ConcertSeat> items = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                ConcertSeat item = new ConcertSeat();
                item.setId(i + 51L);
                item.setConcertScheduleId(r.getArgument(0));
                item.setLabel("일반석");
                item.setPrice(BigDecimal.valueOf(12000));
                item.setSeatNumber(i);
                if (i < 20) {
                    item.setIsActive(false);
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
