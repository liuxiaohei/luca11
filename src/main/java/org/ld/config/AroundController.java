package org.ld.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ld.utils.SnowflakeId;
import org.ld.utils.ZLogger;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
public class AroundController {

    private static final org.slf4j.Logger LOG = ZLogger.newInstance();

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
            LOG.info(snowflakeId + ":CLASS_METHOD : " + point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName());
            return point.proceed();
        } finally {
            AroundController.UUIDS.remove();
        }
    }
}
