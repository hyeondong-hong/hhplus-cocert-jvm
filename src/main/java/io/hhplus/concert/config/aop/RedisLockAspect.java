package io.hhplus.concert.config.aop;

import io.hhplus.concert.config.aop.annotation.RedisLock;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.StringJoiner;

@Slf4j
@Component
@Aspect
@Order(1)  // @Transactional 어노테이션은 @Order(100)으로 설정되어 있어서 트랜잭션 보다 먼저 실행이 보장됨
@AllArgsConstructor
public class RedisLockAspect {

    private final RedissonClient redissonClient;

    @Around("@annotation(redisLock)")
    public Object lockAndExecute(ProceedingJoinPoint joinPoint, RedisLock redisLock) throws Throwable {
        Long threadId = Thread.currentThread().getId();

        String key = redisLock.key() + ":" + getDynamicKey(joinPoint, redisLock.dtoName(), redisLock.fields());
        log.info("Lock 취득 시도: thread-id = {}, key = {}", threadId, key);
        RLock lock = redissonClient.getLock(key);
        try {
            lock.lock();
            log.info("Lock 취득 성공: thread-id = {}, key = {}", threadId, key);
            return joinPoint.proceed();
        } finally {
            lock.unlock();
        }
    }

    private String getDynamicKey(ProceedingJoinPoint joinPoint, String dtoName, String[] fields) throws NoSuchFieldException, IllegalAccessException {
        Object[] args = joinPoint.getArgs();
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();

        if (dtoName == null || dtoName.isEmpty()) {
            // DTO가 없을 경우 메소드 매개변수에서 추출
            StringJoiner joiner = new StringJoiner("&");
            for (String fieldName : fields) {
                boolean fieldFound = false;
                for (int i = 0; i < paramNames.length; i++) {
                    if (paramNames[i].equals(fieldName)) {
                        joiner.add(String.valueOf(args[i]));
                        fieldFound = true;
                        break;
                    }
                }
                if (!fieldFound) {
                    throw new IllegalArgumentException("Field parameter not found: " + fieldName);
                }
            }
            return joiner.toString();
        } else {
            // DTO가 존재할 경우 DTO 필드에서 추출
            for (int i = 0; i < paramNames.length; i++) {
                if (paramNames[i].equals(dtoName)) {
                    Object param = args[i];

                    StringJoiner joiner = new StringJoiner("&");
                    for (String fieldName : fields) {
                        Field field = param.getClass().getDeclaredField(fieldName);
                        field.setAccessible(true);
                        joiner.add(String.valueOf(field.get(param)));
                    }

                    return joiner.toString();
                }
            }
            throw new IllegalArgumentException("Key parameter not found: " + dtoName);
        }
    }
}
