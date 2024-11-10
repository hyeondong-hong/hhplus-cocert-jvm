package io.hhplus.concert.app.user.usecase;

import io.hhplus.concert.app.user.domain.ServiceEntry;
import io.hhplus.concert.app.user.domain.Token;
import io.hhplus.concert.app.user.port.QueueRedisPort;
import io.hhplus.concert.app.user.port.ServiceEntryPort;
import io.hhplus.concert.app.user.port.TokenPort;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@AllArgsConstructor
@Service
public class CheckTokenEnrolledUseCase {

//    private final ServiceEntryPort serviceEntryPort;
    private final TokenPort tokenPort;

    private final QueueRedisPort queueRedisPort;

    public record Input(
            String keyUuid
    ) { }

    public record Output(
            Boolean isAuthenticated,
            Long rank
    ) { }

    @Transactional
    public Output execute(Input input) {
        // 토큰 조회 (없을 경우 BadCredentialsException -> 401)
        Token token = tokenPort.getByKey(input.keyUuid());

        if (token.isExpired()) {
            log.debug("Token expired: uuid = {}", token.getKeyUuid());
            throw new BadCredentialsException("Token Expired");
        }

        Boolean enrolled = queueRedisPort.getEnrolled(input.keyUuid());
        if (!enrolled) {
            // 미진입 상태 -> 403
            Long rank = queueRedisPort.getEntryRank(input.keyUuid());

            if (rank == null) {
                // 미등록 토큰인 경우 등록
                queueRedisPort.entry(input.keyUuid());
                rank = queueRedisPort.getEntryRank(input.keyUuid());
            }

            return new Output(
                    false,
                    rank
            );
        }

        return new Output(
                true,
                null
        );

//        ServiceEntry serviceEntry;
//        if (serviceEntryPort.existsByTokenId(token.getId())) {
//            // 등록된 토큰인 경우 불러오기
//            log.debug("등록된 토큰: token = {}", token.getKeyUuid());
//            serviceEntry = serviceEntryPort.getByTokenId(token.getId());
//        } else {
//            // 미등록 토큰인 경우 등록
//            log.debug("미등록 토큰: token = {}", token.getKeyUuid());
//            serviceEntry = serviceEntryPort.save(
//                    ServiceEntry.builder()
//                            .tokenId(token.getId())
//                            .entryAt(LocalDateTime.now())
//                            .build()
//            );
//        }
//
//        boolean isAuthenticated = true;
//        Long rank = null;
//
//        if (!serviceEntry.isEnrolled()) {
//            // 미진입 상태 -> 403
//            log.debug("진입시도 토큰: {}", token.getKeyUuid());
//            isAuthenticated = false;
//            rank = serviceEntryPort.getEntryRankByTokenId(token.getId());
//        }
//
//        return new Output(
//                isAuthenticated,
//                rank
//        );
    }
}
