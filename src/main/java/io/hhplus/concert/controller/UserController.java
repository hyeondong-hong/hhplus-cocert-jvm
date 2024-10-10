package io.hhplus.concert.controller;

import io.hhplus.concert.usecase.user.ChargeUserPointUseCase;
import io.hhplus.concert.usecase.user.IssueTokenUseCase;
import io.hhplus.concert.usecase.user.RetrieveUserPointUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/a/users")
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
