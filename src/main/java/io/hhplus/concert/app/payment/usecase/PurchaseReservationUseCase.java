package io.hhplus.concert.app.payment.usecase;

import io.hhplus.concert.app.concert.domain.Reservation;
import io.hhplus.concert.app.concert.domain.enm.ReservationStatus;
import io.hhplus.concert.app.concert.port.ConcertPort;
import io.hhplus.concert.app.concert.port.ConcertSchedulePort;
import io.hhplus.concert.app.concert.port.ConcertSeatPort;
import io.hhplus.concert.app.concert.port.ReservationPort;
import io.hhplus.concert.app.payment.domain.Payment;
import io.hhplus.concert.app.payment.domain.PaymentTransaction;
import io.hhplus.concert.app.payment.domain.enm.PaymentMethod;
import io.hhplus.concert.app.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.app.payment.domain.enm.PaymentTransactionStatus;
import io.hhplus.concert.app.payment.port.PaymentPort;
import io.hhplus.concert.app.payment.port.PaymentTransactionPort;
import io.hhplus.concert.app.payment.usecase.dto.PurchaseResult;
import io.hhplus.concert.app.user.domain.PointTransaction;
import io.hhplus.concert.app.user.domain.UserPoint;
import io.hhplus.concert.app.user.domain.enm.PointTransactionStatus;
import io.hhplus.concert.app.user.domain.enm.PointTransactionType;
import io.hhplus.concert.app.user.port.PointTransactionPort;
import io.hhplus.concert.app.user.port.UserPointPort;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Objects;

@AllArgsConstructor
@Service
public class PurchaseReservationUseCase {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

        concertPort.existsOrThrow(input.concertId());
        concertSchedulePort.existsOrThrow(input.concertScheduleId());
        concertSeatPort.existsOrThrow(input.concertSeatId());

        // reservation -> payment 순서대로 비관락
        // 다른 트랜잭션 블록에서 payment -> reservation 순서로 처리 시 데드락에 주의
        Reservation reservation = reservationPort.getWithLock(input.reservationId());
        Payment payment = paymentPort.getByPaymentKeyWithLock(input.paymentKey());
        if (!Objects.equals(reservation.getPaymentId(), payment.getId())) {
            logger.warn("유효하지 않은 결제 키 요청: {} != {}({})",
                    reservation.getPaymentId(), payment.getId(), payment.getPaymentKey());
            throw new IllegalArgumentException("유효하지 않은 결제 키");
        }

        switch (reservation.getStatus()) {
            case COMPLETE -> {
                logger.debug("이미 완료된 예약 결제 요청: reservationId = {}", reservation.getId());
                throw new IllegalStateException("이미 예약이 완료됨");
            }
            case CANCELLED -> {
                logger.debug("취소된 예약 결제 요청: reservationId = {}", reservation.getId());
                throw new IllegalStateException("취소된 예약");
            }
        }

        // 비관락을 걸어야 동시에 접근 시 보유 포인트에 대한 정상적인 차감이 보장됨
        UserPoint userPoint = userPointPort.getByUserIdWithLock(reservation.getUserId());
        if (!userPoint.isEnough(payment.getPrice())) {
            logger.debug("잔여 포인트 부족: userId = {}, remains = {} < amount = {}", userPoint.getUserId(), userPoint.getRemains(), payment.getPrice());
            throw new IllegalStateException("잔여 포인트 부족: (remains = " + userPoint.getRemains() + " < amount = " + payment.getPrice() + ")");
        }
        userPoint.deduct(payment.getPrice());
        userPointPort.save(userPoint);
        pointTransactionPort.save(
                PointTransaction.builder()
                        .userPointId(userPoint.getId())
                        .remains(userPoint.getRemains())
                        .type(PointTransactionType.USED)
                        .status(PointTransactionStatus.COMPLETE)
                        .amount(payment.getPrice().intValue())
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build()
        );
        paymentTransactionPort.save(
                PaymentTransaction.builder()
                        .paymentId(payment.getId())
                        .method(PaymentMethod.CASH)
                        .status(PaymentTransactionStatus.PURCHASE)
                        .amount(payment.getPrice())
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build()
        );

        reservation.setCompleted();
        payment.setPaid();

        reservationPort.save(reservation);
        paymentPort.save(payment);

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
