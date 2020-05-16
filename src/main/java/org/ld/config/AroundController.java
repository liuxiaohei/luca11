package org.ld.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ld.utils.JsonUtil;
import org.ld.utils.ZLogger;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Aspect
@Component
public class AroundController {

    private static final org.slf4j.Logger LOG = ZLogger.newInstance();

    public static final ThreadLocal<String> UUIDS = new ThreadLocal<>();

    /**
     * 打印日志等操作
     */
//    @Around("@within(org.springframework.stereotype.Controller)")
    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object mapResponseBodyAdvice(ProceedingJoinPoint point) throws Throwable {
        final var attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        final var request = Optional.ofNullable(attributes).map(ServletRequestAttributes::getRequest);
        final var shortUUid = Optional.ofNullable(UUIDS.get()).orElseGet(JsonUtil::getShortUuid);
        UUIDS.set(shortUUid);
        LOG.info(shortUUid + ":接口地址:" + request.map(HttpServletRequest::getRequestURI).orElse(null));
        LOG.info(shortUUid + ":请求IP:" + request.map(ServletRequest::getRemoteAddr).orElse(null));
        LOG.info(shortUUid + ":CLASS_METHOD : " + point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName());
        return point.proceed();
    }
}
