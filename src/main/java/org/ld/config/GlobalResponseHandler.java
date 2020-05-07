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
     * 封装会影响swagger的展示
     * so 默认不做封装 只打印一行日志
     */
    @Override
    public Object beforeBodyWrite(Object o,
                                  MethodParameter methodParameter,
                                  MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> aClass,
                                  ServerHttpRequest request,
                                  ServerHttpResponse serverHttpResponse) {
        if (request.getURI().getPath().contains("swagger")) {
            return o; // 防止结构化时swagger 相关的接口被结构化 而不能正常加载资源
        }

        if (o instanceof RespBean) {
            return o;
        }
        try {
            LOG.info(Optional.ofNullable(AroundController.UUIDS.get()).map(e -> e + ":").orElse("") + "Response Body : " + JsonUtil.obj2Json(o));
        } finally {
            AroundController.UUIDS.remove();
        }
        return o;
    }
}
