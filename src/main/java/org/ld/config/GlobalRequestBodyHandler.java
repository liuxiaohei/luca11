package org.ld.config;

import org.ld.utils.JsonUtil;
import org.ld.utils.ZLogger;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.lang.reflect.Type;
import java.util.Optional;

/**
 * 请求体策略
 */
@RestControllerAdvice
public class GlobalRequestBodyHandler implements RequestBodyAdvice {

    private static final org.slf4j.Logger LOG = ZLogger.newInstance();

    @Override
    public boolean supports(
            MethodParameter methodParameter,
            Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public HttpInputMessage beforeBodyRead(
            HttpInputMessage inputMessage,
            MethodParameter parameter,
            Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        return inputMessage;
    }

    @Override
    public Object afterBodyRead(
            Object body,
            HttpInputMessage inputMessage,
            MethodParameter parameter,
            Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        final var shortUUid = Optional.ofNullable(AroundController.UUIDS.get()).orElseGet(JsonUtil::getShortUuid);
        LOG.info(shortUUid + ":RequestBody : {}", JsonUtil.obj2Json(body));
        return body;
    }

    @Override
    public Object handleEmptyBody(
            Object body, HttpInputMessage inputMessage,
            MethodParameter parameter,
            Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

}
