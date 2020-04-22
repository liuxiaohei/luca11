package org.ld.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ld.beans.ResponseBodyBean;
import org.ld.enums.SystemErrorCodeEnum;
import org.ld.exception.CodeStackException;
import org.ld.exception.ErrorCode;
import org.ld.utils.JsonUtil;
import org.ld.utils.LoggerUtil;
import org.ld.utils.UuidUtils;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
@Aspect
@Component
public class AroundController {

    private static final Logger LOG = LoggerUtil.newInstance();

    /**
     * 对Controller的方法进一步进行转化处理
     */
    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object mapResponseBodyAdvice(ProceedingJoinPoint point) {
        final var attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        final var request = attributes.getRequest();
        final var shortUUid = UuidUtils.getShortUuid();
        final var start = System.currentTimeMillis();
        LOG.info(shortUUid + ":接口地址:" + request.getRequestURI());
        LOG.info(shortUUid + ":请求IP:" + request.getRemoteAddr());
        LOG.info(shortUUid + ":CLASS_METHOD : " + point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName());
        LOG.info(shortUUid + ":参数: " + Optional.ofNullable(point.getArgs()).map(Arrays::asList).orElseGet(ArrayList::new).stream().map(JsonUtil::obj2Json).collect(Collectors.joining(",")));
        Object object;
        try {
            final var result = new ResponseBodyBean<>();
            result.setData(point.proceed());
            result.setErrorCode(0);
            result.setMessage("成功");
            LOG.info(shortUUid + ":Response Body : " + JsonUtil.obj2Json(result));
            object = new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Throwable e) {
            LOG.error(shortUUid, e);
            final var se = Optional.of(e)
                    .map(t -> {
                        var t1 = t;
                        while (null != t1) {
                            if (t1 instanceof CodeStackException) return (CodeStackException) t1;//找到第一个CodeException
                            t1 = t1.getCause();
                        }
                        return null;
                    })
                    .orElseGet(() -> SystemErrorCodeEnum.getSystemError(e));
            final var result = new ResponseBodyBean<>();
            result.setErrorCode(Optional.of(se)
                    .map(CodeStackException::getErrorCode)
                    .map(ErrorCode::getCode)
                    .orElseGet(SystemErrorCodeEnum.UNKNOWN::getCode));
            result.setStackTrace(Optional.of(e)
                    .map(error -> {
                        final var sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        return Stream.of(sw.toString().split("\n\t"))
                                .skip(1)
                                .map(str -> str.replace("\n", ""))
                                .toArray(String[]::new);
                    }).orElse(null));
            result.setMessage(Optional.of(se)
                    .map(CodeStackException::getErrorCode)
                    .filter(i -> !i.getCode().equals(SystemErrorCodeEnum.UNKNOWN.getCode()))
                    .map(ErrorCode::getMessage)
                    .orElseGet(e::getMessage));
            object = new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        final var end = System.currentTimeMillis();
        LOG.info(shortUUid + ":执行时间: " + (end - start) + " ms!");
        return object;
    }
}
