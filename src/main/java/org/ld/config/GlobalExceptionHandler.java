package org.ld.config;

import lombok.extern.log4j.Log4j2;
import org.ld.beans.OnErrorResp;
import org.ld.utils.ZLogger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 异常处理
 */
@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final org.slf4j.Logger LOG = ZLogger.newInstance();

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public OnErrorResp exceptionHandler(Throwable e) {
        log.error(AroundController.UUIDS.get(), e);
        return new OnErrorResp(e);
    }
}
