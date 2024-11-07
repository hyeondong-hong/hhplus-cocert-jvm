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
import io.hhplus.concert.config.aop.annotation.RedisLock;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
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
            String keyUuid,
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

    @RedisLock(transactional = true, key = "Point", dtoName = "input", fields = {"keyUuid"})
    public Output execute(Input input) {

        concertPort.existsOrThrow(input.concertId());
        concertSchedulePort.existsOrThrow(input.concertScheduleId());
        concertSeatPort.existsOrThrow(input.concertSeatId());

        Reservation reservation = reservationPort.get(input.reservationId());
        Payment payment = paymentPort.getByPaymentKey(input.paymentKey());
        if (!Objects.equals(reservation.getPaymentId(), payment.getId())) {
            log.warn("유효하지 않은 결제 키 요청: {} != {}({})",
                    reservation.getPaymentId(), payment.getId(), payment.getPaymentKey());
            throw new IllegalArgumentException("유효하지 않은 결제 키");
        }

        switch (reservation.getStatus()) {
            case COMPLETE -> {
                log.debug("이미 완료된 예약 결제 요청: reservationId = {}", reservation.getId());
                throw new IllegalStateException("이미 예약이 완료됨");
            }
            case CANCELLED -> {
                log.debug("취소된 예약 결제 요청: reservationId = {}", reservation.getId());
                throw new IllegalStateException("취소된 예약");
            }
        }

        UserPoint userPoint = userPointPort.getByUserId(reservation.getUserId());
        if (!userPoint.isEnough(payment.getPrice())) {
            log.debug("잔여 포인트 부족: userId = {}, remains = {} < amount = {}", userPoint.getUserId(), userPoint.getRemains(), payment.getPrice());
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
        payment.pay();

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
