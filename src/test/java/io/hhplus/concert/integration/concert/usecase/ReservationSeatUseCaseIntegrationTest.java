package io.hhplus.concert.integration.concert.usecase;

import io.hhplus.concert.concert.domain.Concert;
import io.hhplus.concert.concert.domain.ConcertSchedule;
import io.hhplus.concert.concert.domain.ConcertSeat;
import io.hhplus.concert.concert.domain.Reservation;
import io.hhplus.concert.concert.domain.enm.ReservationStatus;
import io.hhplus.concert.concert.port.ConcertPort;
import io.hhplus.concert.concert.port.ConcertSchedulePort;
import io.hhplus.concert.concert.port.ConcertSeatPort;
import io.hhplus.concert.concert.port.ReservationPort;
import io.hhplus.concert.concert.usecase.ReservationSeatUseCase;
import io.hhplus.concert.payment.domain.Payment;
import io.hhplus.concert.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.payment.port.PaymentPort;
import io.hhplus.concert.user.domain.Token;
import io.hhplus.concert.user.port.TokenPort;
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
        Token token = new Token();
        token.setUserId(1L);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusDays(1));
        tokenPort.save(token);
        uuid = token.getKeyUuid();

        Concert concert = new Concert();
        concert.setTitle("항해99 강좌");
        concert.setCast("항해 코치진");
        concertPort.save(concert);

        ConcertSchedule schedule = new ConcertSchedule();
        schedule.setConcertId(concert.getId());
        schedule.setScheduledAt(LocalDateTime.now().plusDays(1));
        concertSchedulePort.save(schedule);

        ConcertSeat seat = new ConcertSeat();
        seat.setConcertScheduleId(schedule.getId());
        seat.setSeatNumber(1);
        seat.setLabel("일반석");
        seat.setPrice(BigDecimal.valueOf(12000));
        concertSeatPort.save(seat);

        Payment payment = new Payment();
        payment.setPrice(BigDecimal.valueOf(12000));
        payment.setUserId(1L);
        payment.setDueAt(LocalDateTime.now().plusMinutes(5));
        payment.setStatus(PaymentStatus.PENDING);
        paymentPort.save(payment);
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
