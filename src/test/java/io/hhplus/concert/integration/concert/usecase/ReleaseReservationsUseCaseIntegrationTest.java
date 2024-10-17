package io.hhplus.concert.integration.concert.usecase;

import io.hhplus.concert.concert.domain.Reservation;
import io.hhplus.concert.concert.domain.enm.ReservationStatus;
import io.hhplus.concert.concert.port.ReservationPort;
import io.hhplus.concert.concert.usecase.ReleaseReservationsUseCase;
import io.hhplus.concert.payment.domain.Payment;
import io.hhplus.concert.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.payment.port.PaymentPort;
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
    private ReleaseReservationsUseCase releaseReservationsUseCase;

    @Autowired
    private ReservationPort reservationPort;

    @Autowired
    private PaymentPort paymentPort;

    @BeforeEach
    public void setUp() {
        List<Reservation> reservations = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Reservation r = new Reservation();
            r.setPaymentId(i + 1L);
            r.setConcertSeatId(i + 1L);
            r.setStatus(ReservationStatus.PENDING);
            r.setUserId(i + 10L);
            reservations.add(r);
        }
        reservationPort.saveAll(reservations);

        List<Payment> payments = new ArrayList<>();
        for (int i = 0; i < reservations.size() - 5; i++) {
            Payment p = new Payment();
            p.setPrice(BigDecimal.valueOf(12000));
            p.setDueAt(LocalDateTime.now().minusDays(1));
            p.setUserId(1L);
            p.setStatus(PaymentStatus.PENDING);
            p.setPaidAt(null);
            payments.add(p);
        }
        paymentPort.saveAll(payments);
    }

    @Test
    @DisplayName("만료된 결제 정보가 존재하면 해당 결제 정보에 연결된 예약만 취소 처리 한다.")
    public void cancelExpired() {
        releaseReservationsUseCase.execute(new ReleaseReservationsUseCase.Input());
        Payment payment = paymentPort.get(1L);
        assertEquals(payment.getStatus(), PaymentStatus.CANCELLED);
    }
}
