package org.ld.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ld.utils.ControllerUtil;
import org.ld.utils.JsonUtil;
import org.ld.utils.Logger;
import org.ld.utils.UuidUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Aspect
@Component
public class AroundController {

    private static final Logger LOG = Logger.newInstance();

    /**
     * 对Controller的方法进一步进行转化处理
     */
    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object mapResponseBodyAdvice(ProceedingJoinPoint point) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String shortUUid = UuidUtils.getShortUuid();
        long start = System.currentTimeMillis();
        LOG.info(shortUUid + ":接口地址:" + request.getRequestURI());
        LOG.info(shortUUid + ":请求IP:" + request.getRemoteAddr());
        LOG.info(shortUUid + ":CLASS_METHOD : " + point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName());
        LOG.info(shortUUid + ":参数: " + Optional.ofNullable(point.getArgs()).map(Arrays::asList).orElseGet(ArrayList::new).stream().map(JsonUtil::obj2Json).collect(Collectors.joining(",")));
        Object object = ControllerUtil.convertResponseBody(shortUUid,point::proceed);
        long end = System.currentTimeMillis();
        LOG.info(shortUUid + ":执行时间: " + (end - start) + " ms!");
        return object;
    }
}
