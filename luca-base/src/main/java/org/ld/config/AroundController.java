package org.ld.config;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ld.utils.SnowflakeId;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
@Log4j2
public class AroundController {

    public static final ThreadLocal<String> UUIDS = new ThreadLocal<>();

    /**
     * 打印日志等操作
     */
    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object mapResponseBodyAdvice(ProceedingJoinPoint point) throws Throwable {
        try {
            final var snowflakeId = Optional.ofNullable(UUIDS.get()).orElseGet(() -> SnowflakeId.get().toString());
            AroundController.UUIDS.set(snowflakeId);
            UUIDS.set(snowflakeId);
            log.info(snowflakeId + ":CLASS_METHOD : " + point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName());
            return point.proceed();
        } finally {
            AroundController.UUIDS.remove();
        }
    }
}
