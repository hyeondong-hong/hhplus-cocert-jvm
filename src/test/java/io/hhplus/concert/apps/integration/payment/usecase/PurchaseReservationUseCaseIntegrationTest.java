package io.hhplus.concert.apps.integration.payment.usecase;

import io.hhplus.concert.app.concert.domain.Concert;
import io.hhplus.concert.app.concert.domain.ConcertSchedule;
import io.hhplus.concert.app.concert.domain.ConcertSeat;
import io.hhplus.concert.app.concert.domain.Reservation;
import io.hhplus.concert.app.concert.domain.enm.ReservationStatus;
import io.hhplus.concert.app.concert.port.ConcertPort;
import io.hhplus.concert.app.concert.port.ConcertSchedulePort;
import io.hhplus.concert.app.concert.port.ConcertSeatPort;
import io.hhplus.concert.app.concert.port.ReservationPort;
import io.hhplus.concert.app.payment.domain.Payment;
import io.hhplus.concert.app.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.app.payment.port.PaymentPort;
import io.hhplus.concert.app.payment.usecase.PurchaseReservationUseCase;
import io.hhplus.concert.app.user.domain.UserPoint;
import io.hhplus.concert.app.user.port.UserPointPort;
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
public class PurchaseReservationUseCaseIntegrationTest {

    @Autowired
    private IntegrationTestService integrationTestService;

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

        concertSeatPort.save(
                ConcertSeat.builder()
                .concertScheduleId(schedule.getId())
                .seatNumber(1)
                .label("일반석")
                .price(BigDecimal.valueOf(12000))
                .build()
        );

        reservationPort.save(
                Reservation.builder()
                .concertSeatId(1L)
                .userId(1L)
                .status(ReservationStatus.PENDING)
                .paymentId(1L)
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
        paymentKey = payment.getPaymentKey();

        userPointPort.save(
                UserPoint.builder()
                .userId(1L)
                .remains(20000)
                .build()
        );
    }

    @AfterEach
    public void tearDown() {
        integrationTestService.tearDown();
    }

    @Test
    @DisplayName("포인트가 충분한 유저는 예약 결제를 성공한다")
    public void purchaseReservation() {
        PurchaseReservationUseCase.Output output = purchaseReservationUseCase.execute(
                new PurchaseReservationUseCase.Input(
                        "99543f87-9280-45f8-9a56-84a3a3d1312b",
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
