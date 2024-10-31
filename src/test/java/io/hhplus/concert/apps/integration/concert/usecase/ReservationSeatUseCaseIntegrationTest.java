package io.hhplus.concert.apps.integration.concert.usecase;

import io.hhplus.concert.app.concert.domain.Concert;
import io.hhplus.concert.app.concert.domain.ConcertSchedule;
import io.hhplus.concert.app.concert.domain.ConcertSeat;
import io.hhplus.concert.app.concert.domain.Reservation;
import io.hhplus.concert.app.concert.domain.enm.ReservationStatus;
import io.hhplus.concert.app.concert.port.ConcertPort;
import io.hhplus.concert.app.concert.port.ConcertSchedulePort;
import io.hhplus.concert.app.concert.port.ConcertSeatPort;
import io.hhplus.concert.app.concert.port.ReservationPort;
import io.hhplus.concert.app.concert.usecase.ReservationSeatUseCase;
import io.hhplus.concert.app.payment.domain.Payment;
import io.hhplus.concert.app.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.app.payment.port.PaymentPort;
import io.hhplus.concert.app.user.domain.Token;
import io.hhplus.concert.app.user.port.TokenPort;
import io.hhplus.concert.config.IntegrationTestService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class ReservationSeatUseCaseIntegrationTest {

    @Autowired
    private IntegrationTestService integrationTestService;

    @Autowired
    private ReservationSeatUseCase reservationSeatUseCase;

    @Autowired
    private ConcertPort concertPort;

    @Autowired
    private ConcertSchedulePort concertSchedulePort;

    @Autowired
    private ConcertSeatPort concertSeatPort;

    @Autowired
    private ReservationPort reservationPort;

    @Autowired
    private PaymentPort paymentPort;

    @Autowired
    private TokenPort tokenPort;

    private String uuid;

    @BeforeEach
    public void setUp() {
        Token token = tokenPort.save(
                Token.builder()
                        .userId(1L)
                        .createdAt(LocalDateTime.now())
                        .expiresAt(LocalDateTime.now().plusDays(1))
                        .build()
        );
        uuid = token.getKeyUuid();

        Concert concert = concertPort.save(
                Concert.builder()
                        .title("항해99 강좌")
                        .cast("항해 코치진")
                        .build()
        );

        ConcertSchedule schedule = concertSchedulePort.save(
                ConcertSchedule.builder()
                        .concertId(concert.getId())
                        .scheduledAt(LocalDateTime.now().plusDays(1))
                        .build()
        );

        ConcertSeat seat = concertSeatPort.save(
                ConcertSeat.builder()
                        .concertScheduleId(schedule.getId())
                        .seatNumber(1)
                        .label("일반석")
                        .price(BigDecimal.valueOf(12000))
                        .build()
        );

        Payment payment = paymentPort.save(
                Payment.builder()
                        .price(BigDecimal.valueOf(12000))
                        .userId(1L)
                        .dueAt(LocalDateTime.now().plusMinutes(5))
                        .status(PaymentStatus.PENDING)
                        .build()
        );
    }

    @AfterEach
    public void tearDown() {
        integrationTestService.tearDown();
    }

    @Test
    @DisplayName("이미 예약된 좌석이 아니라면 선택한 좌석을 임시 예약 상태로 전환한다")
    public void reserveSeat() {
        ReservationSeatUseCase.Output output = reservationSeatUseCase.execute(
                new ReservationSeatUseCase.Input(
                        uuid,
                        1L,
                        1L,
                        1L
                )
        );

        ConcertSeat seat = concertSeatPort.get(1L);
        assertEquals(false, seat.getIsActive());
        Reservation reservation = reservationPort.get(output.reservationResult().reservationId());
        assertEquals(reservation.getStatus(), ReservationStatus.PENDING);
    }
}
