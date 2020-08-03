package org.ld.config;

import org.ld.beans.RespBean;
import org.ld.enums.SystemErrorCodeEnum;
import org.ld.exception.CodeStackException;
import org.ld.exception.ErrorCode;
import org.ld.utils.ZLogger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    private static final org.slf4j.Logger LOG = ZLogger.newInstance();

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    // TODO 加jwt https://www.cnblogs.com/gdjlc/p/12081701.html
    public RespBean<Object> exceptionHandler(Throwable e) {
        LOG.error(AroundController.UUIDS.get(), e);
        final var se = Optional.of(e)
                .map(t -> {
                    var t1 = t;
                    while (null != t1) {
                        if (t1 instanceof CodeStackException) return (CodeStackException) t1;//找到第一个CodeException
                        t1 = t1.getCause();
                    }
                    return null;
                })
                .orElseGet(() -> new CodeStackException(e));
        final var result = new RespBean<>();
        result.setErrorCode(Optional.of(se)
                .map(CodeStackException::getErrorCode)
                .map(ErrorCode::getCode)
                .orElseGet(SystemErrorCodeEnum.UNKNOWN::getCode));
        result.setStackTrace(Optional.of(e)
                .map(error -> {
                    final var sw = new StringWriter();
                    final var pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    var strings = Stream.of(sw.toString().split("\n\t"))
                            .skip(1)
                            .map(str -> str.replace("\n", ""))
                            .toArray(String[]::new);
                    pw.flush();
                    return strings;
                }).orElse(null));
        result.setErrorMsgDescription(Optional.of(se)
                .map(CodeStackException::getErrorCode)
                .filter(i -> !i.getCode().equals(SystemErrorCodeEnum.UNKNOWN.getCode()))
                .map(ErrorCode::getMessage)
                .orElseGet(e::getMessage));
        result.setMessage(e.getMessage());
        result.setSuccess(false);
        return result;
    }
}
