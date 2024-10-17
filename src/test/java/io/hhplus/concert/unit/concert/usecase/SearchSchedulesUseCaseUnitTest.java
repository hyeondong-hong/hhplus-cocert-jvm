package io.hhplus.concert.unit.concert.usecase;

import io.hhplus.concert.concert.domain.ConcertSchedule;
import io.hhplus.concert.concert.port.ConcertPort;
import io.hhplus.concert.concert.port.ConcertSchedulePort;
import io.hhplus.concert.concert.usecase.SearchSchedulesUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SearchSchedulesUseCaseUnitTest {

    @Mock
    private ConcertPort concertPort;

    @Mock
    private ConcertSchedulePort concertSchedulePort;

    @InjectMocks
    private SearchSchedulesUseCase searchSchedulesUseCase;

    @Test
    @DisplayName("콘서트가 없으면 예외가 발생한다")
    public void noConcerts() {
        when(concertPort.existsById(any(Long.class))).thenReturn(false);

        NoSuchElementException e = assertThrows(
                NoSuchElementException.class, () -> searchSchedulesUseCase.execute(
                        new SearchSchedulesUseCase.Input(
                                1L,
                                PageRequest.of(0, 15)
                        )
                ));

        assertEquals("Concert not found: " + 1L, e.getMessage());
    }

    @Test
    @DisplayName("콘서트 스케줄을 조회한다")
    public void searchSchedules() {
        Pageable pageable = PageRequest.of(0, 15);
        when(concertPort.existsById(any(Long.class))).thenReturn(true);
        when(concertSchedulePort.findAllByConcertIdAndPageable(1L, pageable)).then(r -> {
            List<ConcertSchedule> items = new ArrayList<>();
            for (int i = 0; i < 15; i++) {
                ConcertSchedule item = new ConcertSchedule();
                item.setConcertId(1L);
                item.setId(i + 1L);
                item.setScheduledAt(LocalDateTime.now().plusDays(i));
                items.add(item);
            }
            return new PageImpl<>(items, pageable, 45);
        });

        SearchSchedulesUseCase.Output output = searchSchedulesUseCase.execute(
                new SearchSchedulesUseCase.Input(
                        1L,
                        PageRequest.of(0, 15)
                ));

        assertEquals(15, output.concertScheduleResultPage().getNumberOfElements());
        assertEquals(45, output.concertScheduleResultPage().getTotalElements());
        assertEquals(15, output.concertScheduleResultPage().getContent().get(14).concertScheduleId());
    }
}
