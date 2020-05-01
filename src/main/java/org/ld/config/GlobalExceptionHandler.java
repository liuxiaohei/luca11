package org.ld.config;

import org.ld.beans.ResponseBodyBean;
import org.ld.enums.SystemErrorCodeEnum;
import org.ld.exception.CodeStackException;
import org.ld.exception.ErrorCode;
import org.ld.utils.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 异常处理策略
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerUtil.newInstance();

    // TODO 加jwt https://www.cnblogs.com/gdjlc/p/12081701.html
    @ExceptionHandler(Throwable.class)
    public ResponseBodyBean<Object> exceptionHandler(Throwable e) {
        try {
            LOG.error(AroundController.UUIDS.get(), e);
        } finally {
            AroundController.UUIDS.remove();
        }
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
        result.setSuccess(false);
        return result;
    }
}
