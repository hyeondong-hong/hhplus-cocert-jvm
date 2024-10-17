package io.hhplus.concert.user.usecase;

import io.hhplus.concert.user.port.ServiceEntryPort;
import io.hhplus.concert.user.port.TokenPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@AllArgsConstructor
@Service
public class QueueEjectUseCase {

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

        // 대상 이용자를 토큰 ID 기준으로 방출
        serviceEntryPort.deleteAllByTokenIds(
                Stream.concat(
                        expiredTokens.stream(),
                        expiredEnrolls.stream()
                ).distinct().toList()
        );

        return new Output();
    }
}
