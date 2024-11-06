package io.hhplus.concert.config.aop.component;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionalJoinPoint {
    @Transactional
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}
