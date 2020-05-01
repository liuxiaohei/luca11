package org.ld.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ld.utils.JsonUtil;
import org.ld.utils.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@SuppressWarnings("unused")
@Aspect
@Component
public class AroundController {

    private static final Logger LOG = LoggerUtil.newInstance();

    public static final ThreadLocal<String> UUIDS = new ThreadLocal<>();

    /**
     * 打印日志等操作
     */
    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object mapResponseBodyAdvice(ProceedingJoinPoint point) throws Throwable {
        final var attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        final var request = Optional.ofNullable(attributes).map(ServletRequestAttributes::getRequest);
        final var shortUUid = Optional.ofNullable(UUIDS.get()).orElseGet(JsonUtil::getShortUuid);
        UUIDS.set(shortUUid);
        final var start = System.currentTimeMillis();
        LOG.info(shortUUid + ":接口地址:" + request.map(HttpServletRequest::getRequestURI).orElse(null));
        LOG.info(shortUUid + ":请求IP:" + request.map(ServletRequest::getRemoteAddr).orElse(null));
        LOG.info(shortUUid + ":CLASS_METHOD : " + point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName());
        try {
            var object = point.proceed();
            final var end = System.currentTimeMillis();
            LOG.info(shortUUid + ":执行时间: " + (end - start) + " ms!");
            return object;
        } catch (Throwable e) {
            final var end = System.currentTimeMillis();
            LOG.info(shortUUid + ":执行时间: " + (end - start) + " ms!");
            throw e;
        }
    }
}
