package org.ld.config;

import org.ld.beans.RespBean;
import org.ld.utils.JsonUtil;
import org.ld.utils.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Optional;

@RestControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    private static final Logger LOG = LoggerUtil.newInstance();

    /**
     * 判断支持的类型
     */
    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
    }

    /**
     *
     */
    @Override
    public Object beforeBodyWrite(Object o,
                                  MethodParameter methodParameter,
                                  MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> aClass,
                                  ServerHttpRequest request,
                                  ServerHttpResponse serverHttpResponse) {
        final var path = request.getURI().getPath();
        if (o instanceof RespBean
//                || o instanceof String
                || path.contains("swagger")
                || path.equals("/error")
                || path.equals("/v2/api-docs")
        ) {
            return o; // 防止多余的封装
        }
        try {
            LOG.info(Optional.ofNullable(AroundController.UUIDS.get()).map(e -> e + ":").orElse("") + "Response Body : " + JsonUtil.obj2Json(o));
        } finally {
            AroundController.UUIDS.remove();
        }
        return new RespBean<>(true, o, null, "成功", null, null);
    }
}
