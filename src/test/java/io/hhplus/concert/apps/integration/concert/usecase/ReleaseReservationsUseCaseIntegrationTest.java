package io.hhplus.concert.apps.integration.concert.usecase;

import io.hhplus.concert.app.concert.domain.Reservation;
import io.hhplus.concert.app.concert.domain.enm.ReservationStatus;
import io.hhplus.concert.app.concert.port.ReservationPort;
import io.hhplus.concert.app.concert.usecase.ReleaseReservationsUseCase;
import io.hhplus.concert.app.payment.domain.Payment;
import io.hhplus.concert.app.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.app.payment.port.PaymentPort;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class ReleaseReservationsUseCaseIntegrationTest {

    @Autowired
    private IntegrationTestService integrationTestService;

    @Autowired
    private ReleaseReservationsUseCase releaseReservationsUseCase;

    @Autowired
    private ReservationPort reservationPort;

    @Autowired
    private PaymentPort paymentPort;

    @BeforeEach
    public void setUp() {
        List<Reservation> reservations = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            reservations.add(
                    Reservation.builder()
                            .paymentId(i + 1L)
                            .concertSeatId(i + 1L)
                            .status(ReservationStatus.PENDING)
                            .userId(i + 10L)
                            .build()
            );
        }
        reservationPort.saveAll(reservations);

        List<Payment> payments = new ArrayList<>();
        for (int i = 0; i < reservations.size() - 5; i++) {
            payments.add(
                    Payment.builder()
                            .price(BigDecimal.valueOf(12000))
                            .dueAt(LocalDateTime.now().minusDays(1))
                            .userId(1L)
                            .status(PaymentStatus.PENDING)
                            .paidAt(null)
                            .build()
            );
        }
        paymentPort.saveAll(payments);
    }

    @AfterEach
    public void tearDown() {
        integrationTestService.tearDown();
    }

    @Test
    @DisplayName("만료된 결제 정보가 존재하면 해당 결제 정보에 연결된 예약만 취소 처리 한다.")
    public void cancelExpired() {
        releaseReservationsUseCase.execute(new ReleaseReservationsUseCase.Input());
        Payment payment = paymentPort.get(1L);
        assertEquals(payment.getStatus(), PaymentStatus.CANCELLED);
    }
}
