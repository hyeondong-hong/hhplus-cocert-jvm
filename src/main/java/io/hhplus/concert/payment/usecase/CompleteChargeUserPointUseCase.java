package io.hhplus.concert.payment.usecase;

import io.hhplus.concert.payment.domain.Payment;
import io.hhplus.concert.payment.domain.PaymentTransaction;
import io.hhplus.concert.payment.domain.enm.PaymentMethod;
import io.hhplus.concert.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.payment.domain.enm.PaymentTransactionStatus;
import io.hhplus.concert.payment.domain.enm.PgResultType;
import io.hhplus.concert.payment.port.HangHaePgPort;
import io.hhplus.concert.payment.port.PaymentPort;
import io.hhplus.concert.payment.port.PaymentTransactionPort;
import io.hhplus.concert.payment.usecase.dto.PointChangeResult;
import io.hhplus.concert.user.domain.PointTransaction;
import io.hhplus.concert.user.domain.UserPoint;
import io.hhplus.concert.user.domain.enm.PointTransactionStatus;
import io.hhplus.concert.user.domain.enm.PointTransactionType;
import io.hhplus.concert.user.port.PointTransactionPort;
import io.hhplus.concert.user.port.UserPointPort;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@AllArgsConstructor
@Service
public class CompleteChargeUserPointUseCase {

    private final HangHaePgPort hangHaePgPort;

    private final PaymentPort paymentPort;
    private final PaymentTransactionPort paymentTransactionPort;
    private final UserPointPort userPointPort;
    private final PointTransactionPort pointTransactionPort;

    public record Input(
            Long userId,
            String paymentKey
    ) { }

    public record Output(
            PointChangeResult pointChangeResult
    ) { }

    @Transactional
    public Output execute(Input input) {
        Payment payment = paymentPort.getByPaymentKeyWithLock(input.paymentKey());
        switch (payment.getStatus()) {
            case PAID -> throw new IllegalArgumentException("이미 완료된 결제");
            case CANCELLED -> throw new IllegalArgumentException("취소된 결제");
        }
        PointTransaction pointTransaction = pointTransactionPort.getByPaymentId(payment.getId());

        if (payment.getDueAt().isBefore(LocalDateTime.now())) {
            pointTransaction.setStatus(PointTransactionStatus.CANCELLED);
            payment.setStatus(PaymentStatus.CANCELLED);
            pointTransactionPort.save(pointTransaction);
            paymentPort.save(payment);
            throw new IllegalArgumentException("결제 기한 만료");
        }

        UserPoint userPoint = userPointPort.getByUserIdWithLock(input.userId());

        PgResultType result = hangHaePgPort.purchase(
                payment.getUserId(),
                input.paymentKey(),
                payment.getPrice()
        );

        if (result != PgResultType.OK) {
            switch (result) {
                case INVALID -> throw new IllegalArgumentException("사용 불가한 결제 정보");
                case ALREADY_PURCHASED -> throw new IllegalArgumentException("이미 완료된 결제");
                case REJECTED_PAYMENT -> throw new AccessDeniedException("승인 거부");
                default -> throw new RuntimeException("결제 시스템 오류");
            }
        }

        userPoint.setRemains(userPoint.getRemains() + pointTransaction.getAmount());

        pointTransaction.setRemains(userPoint.getRemains());
        pointTransaction.setStatus(PointTransactionStatus.COMPLETE);

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setPaymentId(payment.getId());
        paymentTransaction.setMethod(PaymentMethod.POINT);
        paymentTransaction.setStatus(PaymentTransactionStatus.PURCHASE);
        paymentTransaction.setAmount(payment.getPrice());
        paymentTransaction.setCreatedAt(LocalDateTime.now());
        paymentTransaction.setModifiedAt(LocalDateTime.now());

        pointTransaction = pointTransactionPort.save(pointTransaction);
        userPoint = userPointPort.save(userPoint);
        paymentPort.save(payment);
        paymentTransactionPort.save(paymentTransaction);

        return new Output(
                new PointChangeResult(
                        userPoint.getUserId(),
                        userPoint.getRemains(),
                        PointTransactionType.CHARGE,
                        pointTransaction.getAmount()
                )
        );
    }

    /* NOTE: 이하 멱등성 처리 관련 로직...
     PG사 모듈의 동시성 처리 및 트랜잭션 처리를 위한 구조적인 지저분함 때문에 우선 주석처리.

    public record Prepared(
            PgResultType pgResultType,
            UserPoint userPoint,
            PointTransaction pointTransaction,
            Payment payment,
            PaymentTransaction paymentTransaction
    ) { }

    // 서드파티 PG사 모듈 사용 시나리오를 가정하고 트랜잭션 설정하지 않음
    public Prepared prepare(Input input) {
        // NOTE: 여기선 트랜잭션 블록이 아니므로 `getByPaymentKeyWithLock`이 아니라 `getByPaymentKey`로 조회
        PaymentTransaction paymentTransaction = paymentTransactionPort.getByPaymentKey(input.paymentKey());
        if (paymentTransaction.getIsComplete()) {
            throw new IllegalArgumentException("이미 완료된 결제");
        }
        Payment payment = paymentPort.get(paymentTransaction.getPaymentId());
        PointTransaction pointTransaction = pointTransactionPort.getByPaymentId(paymentTransaction.getPaymentId());

        if (payment.getDueAt().isBefore(LocalDateTime.now())) {
            pointTransaction.setStatus(PointTransactionStatus.CANCELLED);
            payment.setStatus(PaymentStatus.CANCELLED);
            pointTransactionPort.save(pointTransaction);
            paymentPort.save(payment);
            throw new IllegalArgumentException("결제 기한 만료");
        }

        UserPoint userPoint = userPointPort.getByUserId(input.userId());

        // PG사 시스템 내에서 동시에 접근한 결제 처리는 멱등성이 보장됨 (`paymentKey`에 의해)
        PgResultType result = hangHaePgPort.purchase(
                payment.getUserId(),
                input.paymentKey(),
                paymentTransaction.getAmount()
        );

        return new Prepared(
                result,
                userPoint,
                pointTransaction,
                payment,
                paymentTransaction
        );
    }

    // 데이터 무결성 처리를 위해 트랜잭션 블록 분리
    @Transactional
    public Output performComplete(Prepared prepared) {

        if (prepared.pgResultType() != PgResultType.OK) {
            switch (prepared.pgResultType()) {
                case INVALID -> throw new IllegalArgumentException("사용 불가한 결제 정보");
                case ALREADY_PURCHASED -> throw new IllegalArgumentException("이미 완료된 결제");
                case REJECTED_PAYMENT -> throw new AccessDeniedException("승인 거부");
                default -> throw new RuntimeException("결제 시스템 오류");
            }
        }

        prepared.userPoint().setRemains(prepared.userPoint().getRemains() + prepared.pointTransaction().getAmount());
        prepared.pointTransaction().setRemains(prepared.userPoint().getRemains());
        prepared.pointTransaction().setStatus(PointTransactionStatus.COMPLETE);
        prepared.payment().setStatus(PaymentStatus.PAID);
        prepared.payment().setPaidAt(LocalDateTime.now());
        prepared.paymentTransaction().setIsComplete(true);
        prepared.paymentTransaction().setModifiedAt(LocalDateTime.now());
        PointTransaction pointTransaction = pointTransactionPort.save(prepared.pointTransaction());
        UserPoint userPoint = userPointPort.save(prepared.userPoint());
        paymentPort.save(prepared.payment());
        paymentTransactionPort.save(prepared.paymentTransaction());

        return new Output(
                new PointChangeResult(
                        userPoint.getUserId(),
                        userPoint.getRemains(),
                        PointTransactionType.CHARGE,
                        pointTransaction.getAmount()
                )
        );
    }

    // 트랜잭션 블록에서 오류 발생 시 환불 처리
    public void onError(Input input) {
        PaymentTransaction paymentTransaction = paymentTransactionPort.getByPaymentKey(input.paymentKey());
        Payment payment = paymentPort.get(paymentTransaction.getPaymentId());

        hangHaePgPort.refund(
                payment.getUserId(),
                input.paymentKey(),
                paymentTransaction.getAmount()
        );
    }

     */
}
