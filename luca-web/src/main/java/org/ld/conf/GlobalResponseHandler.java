package org.ld.conf;

import org.ld.beans.RespBean;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * ld
 * 返回值转换策略
 */
@RestControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    /**
     * 判断支持的类型
     */
    @Override
    public boolean supports(MethodParameter methodParameter,
                            Class<? extends HttpMessageConverter<?>> aClass) {
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
        final String path = request.getURI().getPath();
        if (o instanceof RespBean                  // 不包装已包装的类
                || o instanceof FileSystemResource // 不包装文件流
                || path.contains("swagger")        // 不包装swagger相关的接口
                || path.equals("/error")
                || path.equals("/v2/api-docs")
        ) {
            return o; // 防止多余的封装 防止swagger 也被封装 影响swagger界面显示
        }
        return new RespBean<>(o);
    }
}
