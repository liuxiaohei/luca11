package org.ld.conf;

import org.ld.beans.RespBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * ld
 * 异常处理策略
 */
@RestControllerAdvice
public class WebGlobalExceptionHandler {

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RespBean<Object> exceptionHandler(Throwable e) {
        return new RespBean<>(e);
    }
}
