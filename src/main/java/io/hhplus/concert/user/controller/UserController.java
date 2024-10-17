package io.hhplus.concert.user.controller;

import io.hhplus.concert.user.usecase.ChargeUserPointUseCase;
import io.hhplus.concert.user.usecase.IssueTokenUseCase;
import io.hhplus.concert.user.usecase.RetrieveUserPointUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/1.0/users")
public class UserController {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{userId}/tokens")
    public IssueTokenUseCase.Output issueToken(
            @PathVariable Long userId
    ) {
        return new IssueTokenUseCase.Output(
                "token-info-long-hashed-text"
        );
    }

    @GetMapping("/{userId}/point")
    public RetrieveUserPointUseCase.Output retrieveUserPoint(
            @PathVariable Long userId
    ) {
        return new RetrieveUserPointUseCase.Output(
                userId,
                1000
        );
    }

    @PatchMapping("/{userId}/point")
    public ChargeUserPointUseCase.Output chargeUserPoint(
            @PathVariable Long userId,
            @RequestBody ChargeUserPointUseCase.Input input
    ) {
        return new ChargeUserPointUseCase.Output(
                userId,
                2000,
                1000
        );
    }
}
