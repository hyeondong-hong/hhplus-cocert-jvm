package io.hhplus.concert.app.user.usecase;

import io.hhplus.concert.app.user.port.ServiceEntryPort;
import io.hhplus.concert.app.user.port.TokenPort;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@AllArgsConstructor
@Service
public class QueueEjectUseCase {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ServiceEntryPort serviceEntryPort;
    private final TokenPort tokenPort;

    public record Input(
    ) { }

    public record Output(
    ) { }

    @Transactional
    public Output execute(Input input) {
        // 토큰이 만료된 이용자 토큰 ID
        List<Long> expiredTokens = tokenPort.findAllIdsExpiredWithLock();

        // 등록 후 10분이 지난 이용자 토큰 ID
        List<Long> expiredEnrolls = serviceEntryPort.findAllTokenIdExpiredWithLock();

        List<Long> ejectTargets = Stream.concat(
                expiredTokens.stream(),
                expiredEnrolls.stream()
        ).distinct().toList();

        logger.info("Schedule: Ejected: {}", ejectTargets);

        // 대상 이용자를 토큰 ID 기준으로 방출
        serviceEntryPort.deleteAllByTokenIds(ejectTargets);

        return new Output();
    }
}
