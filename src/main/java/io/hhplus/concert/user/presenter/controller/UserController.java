package io.hhplus.concert.user.presenter.controller;

import io.hhplus.concert.payment.usecase.CompleteChargeUserPointUseCase;
import io.hhplus.concert.payment.usecase.PendingChargeUserPointUseCase;
import io.hhplus.concert.payment.usecase.dto.PendingPointChargeResult;
import io.hhplus.concert.payment.usecase.dto.PointChangeResult;
import io.hhplus.concert.user.usecase.IssueTokenUseCase;
import io.hhplus.concert.user.usecase.RetrieveUserPointUseCase;
import io.hhplus.concert.user.usecase.dto.ChargePointResult;
import io.hhplus.concert.user.usecase.dto.TokenResult;
import io.hhplus.concert.user.usecase.dto.UserPointResult;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/1.0/users")
public class UserController {

    private final IssueTokenUseCase issueTokenUseCase;
    private final RetrieveUserPointUseCase retrieveUserPointUseCase;
    private final PendingChargeUserPointUseCase pendingChargeUserPointUseCase;
    private final CompleteChargeUserPointUseCase completeChargeUserPointUseCase;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{userId}/tokens")
    public TokenResult issueToken(
            @PathVariable Long userId
    ) {
        IssueTokenUseCase.Output output = issueTokenUseCase.execute(
                new IssueTokenUseCase.Input(
                        userId
                )
        );
        return output.tokenResult();
    }

    @GetMapping("/{userId}/points")
    public UserPointResult retrieveUserPoint(
            @PathVariable Long userId
    ) {
        RetrieveUserPointUseCase.Output output = retrieveUserPointUseCase.execute(
                new RetrieveUserPointUseCase.Input(
                        userId
                )
        );
        return output.userPointResult();
    }

    @PostMapping("/{userId}/points/pending")
    public PendingPointChargeResult pendingChargeUserPoint(
            @PathVariable @Min(1) Long userId,
            @RequestBody ChargePointResult requestBody
    ) {
        PendingChargeUserPointUseCase.Output output = pendingChargeUserPointUseCase.execute(
                new PendingChargeUserPointUseCase.Input(
                        userId,
                        requestBody.chargeAmount()
                )
        );
        return output.pendingPointChargeResult();
    }

    @PatchMapping("/{userId}/points")
    public PointChangeResult completeChargeUserPoint(
            @PathVariable @Min(1) Long userId,
            @RequestParam String paymentKey
    ) {
        CompleteChargeUserPointUseCase.Output output = completeChargeUserPointUseCase.execute(
                new CompleteChargeUserPointUseCase.Input(
                        userId,
                        paymentKey
                )
        );
        return output.pointChangeResult();

        /* NOTE: CompleteChargeUserPointUseCase 클래스 내 주석처리된 멱등성 처리 로직 실행 절차

        CompleteChargeUserPointUseCase.Input input = new CompleteChargeUserPointUseCase.Input(
                userId,
                paymentKey
        );
        CompleteChargeUserPointUseCase.Prepared prepared = completeChargeUserPointUseCase.prepare(
                input
        );
        // Transactional
        try {
            CompleteChargeUserPointUseCase.Output output = completeChargeUserPointUseCase.performComplete(
                    prepared
            );
            return output.pointChangeResult();
        } catch (RuntimeException e) {
            // 환불처리
            completeChargeUserPointUseCase.onError(input);
            throw e;
        }
         */
    }
}
