package io.hhplus.concert.payment.usecase;

import io.hhplus.concert.concert.domain.Reservation;
import io.hhplus.concert.concert.domain.enm.ReservationStatus;
import io.hhplus.concert.concert.port.ConcertPort;
import io.hhplus.concert.concert.port.ConcertSchedulePort;
import io.hhplus.concert.concert.port.ConcertSeatPort;
import io.hhplus.concert.concert.port.ReservationPort;
import io.hhplus.concert.payment.domain.Payment;
import io.hhplus.concert.payment.domain.PaymentTransaction;
import io.hhplus.concert.payment.domain.enm.PaymentMethod;
import io.hhplus.concert.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.payment.domain.enm.PaymentTransactionStatus;
import io.hhplus.concert.payment.port.PaymentPort;
import io.hhplus.concert.payment.port.PaymentTransactionPort;
import io.hhplus.concert.payment.usecase.dto.PurchaseResult;
import io.hhplus.concert.user.domain.PointTransaction;
import io.hhplus.concert.user.domain.UserPoint;
import io.hhplus.concert.user.domain.enm.PointTransactionStatus;
import io.hhplus.concert.user.domain.enm.PointTransactionType;
import io.hhplus.concert.user.port.PointTransactionPort;
import io.hhplus.concert.user.port.UserPointPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Objects;

@AllArgsConstructor
@Service
public class PurchaseReservationUseCase {

    private final PaymentPort paymentPort;
    private final PaymentTransactionPort paymentTransactionPort;
    private final UserPointPort userPointPort;
    private final ConcertPort concertPort;
    private final ConcertSchedulePort concertSchedulePort;
    private final ConcertSeatPort concertSeatPort;
    private final ReservationPort reservationPort;
    private final PointTransactionPort pointTransactionPort;

    public record Input(
            Long concertId,
            Long concertScheduleId,
            Long concertSeatId,
            Long reservationId,
            String paymentKey
    ) { }

    public record Output(
            ReservationStatus reservationStatus,
            PurchaseResult purchaseResult
    ) { }

    @Transactional
    public Output execute(Input input) {

        if (!concertPort.existsById(input.concertId())) {
            throw new NoSuchElementException("Concert not found: " + input.concertId());
        }

        if (!concertSchedulePort.existsById(input.concertScheduleId())) {
            throw new NoSuchElementException("Concert Schedule not found: " + input.concertScheduleId());
        }

        if (!concertSeatPort.existsById(input.concertSeatId())) {
            throw new NoSuchElementException("Concert Seat not found: " + input.concertSeatId());
        }

        // reservation -> payment 순서대로 비관락
        // 다른 트랜잭션 블록에서 payment -> reservation 순서로 처리 시 데드락에 주의
        Reservation reservation = reservationPort.getWithLock(input.reservationId());
        Payment payment = paymentPort.getByPaymentKeyWithLock(input.paymentKey());
        if (!Objects.equals(reservation.getPaymentId(), payment.getId())) {
            throw new IllegalArgumentException("유효하지 않은 결제 키");
        } else if (reservation.getStatus() == ReservationStatus.COMPLETE) {
            throw new IllegalStateException("이미 예약이 완료됨");
        } else if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("취소된 예약");
        }

        // 비관락을 걸어야 동시에 접근 시 보유 포인트에 대한 정상적인 차감이 보장됨
        UserPoint userPoint = userPointPort.getByUserIdWithLock(reservation.getUserId());
        BigDecimal remainsDecimal = BigDecimal.valueOf(userPoint.getRemains());
        // 0, 1 = 결제 가능, -1 = 포인트 부족
        if (remainsDecimal.compareTo(payment.getPrice()) >= 0) {
            Integer priceInteger = payment.getPrice().intValue();

            userPoint.setRemains(userPoint.getRemains() - priceInteger);
            userPointPort.save(userPoint);

            PointTransaction pointTransaction = new PointTransaction();
            pointTransaction.setUserPointId(userPoint.getId());
            pointTransaction.setRemains(userPoint.getRemains());
            pointTransaction.setType(PointTransactionType.USED);
            pointTransaction.setStatus(PointTransactionStatus.COMPLETE);
            pointTransaction.setAmount(priceInteger);
            pointTransaction.setCreatedAt(LocalDateTime.now());
            pointTransaction.setModifiedAt(LocalDateTime.now());
            pointTransactionPort.save(pointTransaction);

            PaymentTransaction paymentTransaction = new PaymentTransaction();
            paymentTransaction.setPaymentId(payment.getId());
            paymentTransaction.setMethod(PaymentMethod.CASH);
            paymentTransaction.setStatus(PaymentTransactionStatus.PURCHASE);
            paymentTransaction.setAmount(payment.getPrice());
            paymentTransaction.setCreatedAt(LocalDateTime.now());
            paymentTransaction.setModifiedAt(LocalDateTime.now());
            paymentTransactionPort.save(paymentTransaction);

            reservation.setStatus(ReservationStatus.COMPLETE);
            reservationPort.save(reservation);

            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            paymentPort.save(payment);
        }

        return new Output(
                reservation.getStatus(),
                new PurchaseResult(
                        payment.getId(),
                        payment.getUserId(),
                        payment.getPrice(),
                        payment.getStatus(),
                        payment.getDueAt(),
                        payment.getPaidAt()
                )
        );
    }
}
