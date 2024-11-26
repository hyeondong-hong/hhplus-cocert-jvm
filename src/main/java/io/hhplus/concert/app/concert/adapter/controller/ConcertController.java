package io.hhplus.concert.app.concert.adapter.controller;

import io.hhplus.concert.app.concert.usecase.ReservationSeatUseCase;
import io.hhplus.concert.app.concert.usecase.SearchAvailableSeatsUseCase;
import io.hhplus.concert.app.concert.usecase.SearchConcertsUseCase;
import io.hhplus.concert.app.concert.usecase.SearchSchedulesUseCase;
import io.hhplus.concert.app.concert.usecase.dto.ConcertResult;
import io.hhplus.concert.app.concert.usecase.dto.ConcertScheduleResult;
import io.hhplus.concert.app.concert.usecase.dto.ConcertSeatResult;
import io.hhplus.concert.app.concert.usecase.dto.ReservationResult;
import io.hhplus.concert.app.payment.usecase.PurchaseReservationUseCase;
import io.hhplus.concert.app.payment.usecase.dto.PurchaseResult;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/1.0/concerts")
public class ConcertController {

    private final PurchaseReservationUseCase purchaseReservationUseCase;
    private final ReservationSeatUseCase reservationSeatUseCase;
    private final SearchConcertsUseCase searchConcertsUseCase;
    private final SearchSchedulesUseCase searchSchedulesUseCase;
    private final SearchAvailableSeatsUseCase searchAvailableSeatsUseCase;

    @GetMapping("")
    public Page<ConcertResult> searchConcerts(
            @PageableDefault(size = 15, sort = "id") Pageable pageable
    ) {
        return searchConcertsUseCase.execute(
                new SearchConcertsUseCase.Input(
                        pageable
                )
        ).concertResultPage();
    }

    @GetMapping("/{concertId}/schedules")
    public Page<ConcertScheduleResult> searchSeats(
            @PageableDefault(size = 15, sort = "id") Pageable pageable,
            @PathVariable @Min(1) Long concertId
    ) {
        return searchSchedulesUseCase.execute(
                new SearchSchedulesUseCase.Input(
                        concertId,
                        pageable
                )
        ).concertScheduleResultPage();
    }

    @GetMapping("/{concertId}/schedules/{concertScheduleId}/seats")
    public List<ConcertSeatResult> searchSchedules(
            @PathVariable @Min(1) Long concertId,
            @PathVariable @Min(1) Long concertScheduleId
    ) {
        SearchAvailableSeatsUseCase.Output output = searchAvailableSeatsUseCase.execute(
                new SearchAvailableSeatsUseCase.Input(
                        concertId,
                        concertScheduleId
                )
        );

        return output.seatResults();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{concertId}/schedules/{concertScheduleId}/seats/{concertSeatId}/reservations/pending")
    public ReservationResult reservationSeat(
            @RequestHeader("Authorization") String keyUuid,
            @PathVariable @Min(1) Long concertId,
            @PathVariable @Min(1) Long concertScheduleId,
            @PathVariable @Min(1) Long concertSeatId
    ) {
        ReservationSeatUseCase.Output reservationOutput = reservationSeatUseCase.execute(
                new ReservationSeatUseCase.Input(
                        keyUuid,
                        concertId,
                        concertScheduleId,
                        concertSeatId
                )
        );

        return reservationOutput.reservationResult();
    }

    @PatchMapping("/{concertId}/schedules/{concertScheduleId}/seats/{concertSeatId}/reservations/{reservationId}/payment")
    public PurchaseResult purchase(
            @RequestHeader("Authorization") String keyUuid,
            @PathVariable @Min(1) Long concertId,
            @PathVariable @Min(1) Long concertScheduleId,
            @PathVariable @Min(1) Long concertSeatId,
            @PathVariable @Min(1) Long reservationId,
            @RequestParam String paymentKey
    ) {
        PurchaseReservationUseCase.Output output = purchaseReservationUseCase.execute(
                new PurchaseReservationUseCase.Input(
                        keyUuid,
                        concertId,
                        concertScheduleId,
                        concertSeatId,
                        reservationId,
                        paymentKey
                )
        );
        return output.purchaseResult();
    }
}
