package io.hhplus.concert.payment.presenter.scheduler;

import io.hhplus.concert.concert.usecase.ReleaseReservationsUseCase;
import io.hhplus.concert.user.usecase.ReleasePointTransactionsUseCase;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class ReleasePaymentsScheduler {

    private final ReleaseReservationsUseCase releaseReservationsUseCase;
    private final ReleasePointTransactionsUseCase releasePointTransactionsUseCase;

    @Scheduled(cron = "0 * * * * *")
    public void releaseReservations() {
        releaseReservationsUseCase.execute(new ReleaseReservationsUseCase.Input());
    }

    @Scheduled(cron = "0 * * * * *")
    public void releasePointTransactions() {
        releasePointTransactionsUseCase.execute(new ReleasePointTransactionsUseCase.Input());
    }
}
