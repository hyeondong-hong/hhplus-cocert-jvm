package io.hhplus.concert.unit.concert.usecase;

import io.hhplus.concert.app.concert.domain.Concert;
import io.hhplus.concert.app.concert.port.ConcertPort;
import io.hhplus.concert.app.concert.usecase.SearchConcertsUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SearchConcertsUseCaseUnitTest {

    @Mock
    private ConcertPort concertPort;

    @InjectMocks
    private SearchConcertsUseCase searchConcertsUseCase;

    @Test
    @DisplayName("콘서트를 조회한다")
    public void searchConcerts() {
        Pageable pageable = PageRequest.of(0, 15);
        when(concertPort.findAllByPageable(pageable)).then(r -> {
            List<Concert> items = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                items.add(
                        Concert.builder()
                                .id(i + 1L)
                                .title("항해99 특강" + i)
                                .cast("항해 코치진")
                                .build()
                );
            }
            return new PageImpl<>(items, pageable, 7);
        });

        SearchConcertsUseCase.Output output = searchConcertsUseCase.execute(
                new SearchConcertsUseCase.Input(
                        PageRequest.of(0, 15)
                ));

        assertEquals(5, output.concertResultPage().getNumberOfElements());
        assertEquals(5, output.concertResultPage().getTotalElements());
        assertEquals(5, output.concertResultPage().getContent().get(4).concertId());
    }
}
