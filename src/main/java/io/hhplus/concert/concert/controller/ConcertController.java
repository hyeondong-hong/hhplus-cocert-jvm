package io.hhplus.concert.concert.controller;

import io.hhplus.concert.concert.usecase.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/1.0/concerts")
public class ConcertController {

    @GetMapping("")
    public List<SearchConcertUseCase.Output> searchConcerts() {
        return List.of(
                new SearchConcertUseCase.Output(1L, "콘서트1"),
                new SearchConcertUseCase.Output(2L, "콘서트2"),
                new SearchConcertUseCase.Output(3L, "콘서트3"),
                new SearchConcertUseCase.Output(4L, "콘서트4")
        );
    }

    @GetMapping("/{concertId}/schedules")
    public List<SearchSchedulesUseCase.Output> searchSeats(
            @PathVariable Long concertId
    ) {
        return List.of(
                new SearchSchedulesUseCase.Output(
                        concertId,
                        1L,
                        LocalDateTime.now().plusDays(1L)
                ),
                new SearchSchedulesUseCase.Output(
                        concertId,
                        2L,
                        LocalDateTime.now().plusDays(2L)
                ),
                new SearchSchedulesUseCase.Output(
                        concertId,
                        3L,
                        LocalDateTime.now().plusDays(3L)
                )
        );
    }

    @GetMapping("/{concertId}/schedules/{concertScheduleId}/seats")
    public SearchSeatsUseCase.Output searchSchedules(
            @PathVariable Long concertId,
            @PathVariable Long concertScheduleId
    ) {
        return new SearchSeatsUseCase.Output(
                concertScheduleId,
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        );
    }

    @PostMapping("/{concertId}/schedules/{concertScheduleId}/reservations")
    public ReservationSeatUseCase.Output reservationSeat(
            @PathVariable Long concertId,
            @PathVariable Long concertScheduleId,
            @RequestBody ReservationSeatUseCase.Input input
    ) {
        return new ReservationSeatUseCase.Output(
                1L,
                2L,
                9,
                "temp",
                10L,
                "pending"
        );
    }

    @PatchMapping("/{concertId}/schedules/{concertScheduleId}/reservations/{reservationId}")
    public PurchaseReservationUseCase.Output purchase(
            @PathVariable Long concertId,
            @PathVariable Long concertScheduleId,
            @PathVariable Long reservationId,
            @RequestBody PurchaseReservationUseCase.Input input
    ) {
        return new PurchaseReservationUseCase.Output(
                reservationId,
                1L,
                "paid"
        );
    }
}
