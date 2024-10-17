package io.hhplus.concert.user.presenter.scheduler;

import io.hhplus.concert.user.usecase.QueueEjectUseCase;
import io.hhplus.concert.user.usecase.QueueEnrollUseCase;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class TokenQueueScheduler {

    private final QueueEjectUseCase queueEjectUseCase;
    private final QueueEnrollUseCase queueEnrollUseCase;

    @Scheduled(cron = "0 * * * * *")
    public void queueEject() {
        queueEjectUseCase.execute(new QueueEjectUseCase.Input());
    }

    @Scheduled(cron = "0 * * * * *")
    public void queueEnroll() {
        queueEnrollUseCase.execute(new QueueEnrollUseCase.Input());
    }
}
