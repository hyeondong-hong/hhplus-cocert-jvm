package io.hhplus.concert.app.user.usecase;

import io.hhplus.concert.app.user.port.ServiceEntryPort;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class QueueEnrollUseCase {

    private final ServiceEntryPort serviceEntryPort;

    public record Input(
    ) { }

    public record Output(
    ) { }

    @Transactional
    public Output execute(Input input) {

        // 등록 가능한 진입 이용자 토큰 ID (30명)
        List<Long> enrollableTokenIds = serviceEntryPort.findEnrollableAllTokenIdByTop30();

        log.debug("Schedule: Enrolled: {}", enrollableTokenIds);

        // 진입 이용자를 서비스 사용 가능하도록 등록
        serviceEntryPort.enrollAllByTokenIds(enrollableTokenIds);

        return new Output();
    }
}
