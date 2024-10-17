package io.hhplus.concert.integration.payment.usecase;

import io.hhplus.concert.concert.domain.Concert;
import io.hhplus.concert.concert.domain.ConcertSchedule;
import io.hhplus.concert.concert.domain.ConcertSeat;
import io.hhplus.concert.concert.domain.Reservation;
import io.hhplus.concert.concert.domain.enm.ReservationStatus;
import io.hhplus.concert.concert.port.ConcertPort;
import io.hhplus.concert.concert.port.ConcertSchedulePort;
import io.hhplus.concert.concert.port.ConcertSeatPort;
import io.hhplus.concert.concert.port.ReservationPort;
import io.hhplus.concert.payment.domain.Payment;
import io.hhplus.concert.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.payment.port.PaymentPort;
import io.hhplus.concert.payment.usecase.PurchaseReservationUseCase;
import io.hhplus.concert.user.domain.UserPoint;
import io.hhplus.concert.user.port.UserPointPort;
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
public class PurchaseReservationUseCaseIntegrationTest {

    @Autowired
    private PurchaseReservationUseCase purchaseReservationUseCase;

    @Autowired
    private PaymentPort paymentPort;

    @Autowired
    private UserPointPort userPointPort;

    @Autowired
    private ConcertPort concertPort;

    @Autowired
    private ConcertSchedulePort concertSchedulePort;

    @Autowired
    private ConcertSeatPort concertSeatPort;

    @Autowired
    private ReservationPort reservationPort;

    private String paymentKey;

    @BeforeEach
    public void setUp() {
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

        Reservation reservation = new Reservation();
        reservation.setConcertSeatId(1L);
        reservation.setUserId(1L);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setPaymentId(1L);
        reservationPort.save(reservation);

        Payment payment = new Payment();
        payment.setPrice(BigDecimal.valueOf(12000));
        payment.setUserId(1L);
        payment.setDueAt(LocalDateTime.now().plusMinutes(5));
        payment.setStatus(PaymentStatus.PENDING);
        paymentPort.save(payment);
        paymentKey = payment.getPaymentKey();

        UserPoint userPoint = new UserPoint();
        userPoint.setUserId(1L);
        userPoint.setRemains(20000);
        userPointPort.save(userPoint);
    }

    @Test
    @DisplayName("포인트가 충분한 유저는 예약 결제를 성공한다")
    public void purchaseReservation() {
        PurchaseReservationUseCase.Output output = purchaseReservationUseCase.execute(
                new PurchaseReservationUseCase.Input(
                        1L,
                        1L,
                        1L,
                        1L,
                        paymentKey
                )
        );

        UserPoint userPoint = userPointPort.getByUserId(1L);

        assertEquals(ReservationStatus.COMPLETE, output.reservationStatus());
        assertEquals(PaymentStatus.PAID, output.purchaseResult().paymentStatus());
        assertEquals(8000, userPoint.getRemains());
    }
}
