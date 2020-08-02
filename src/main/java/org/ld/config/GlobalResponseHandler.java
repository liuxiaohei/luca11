package org.ld.config;

import com.google.common.base.Preconditions;
import org.ld.beans.RespBean;
import org.ld.utils.JsonUtil;
import org.ld.utils.ZLogger;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

//https://xbuba.com/questions/48705172
//https://blog.csdn.net/lizz861109/article/details/106303929
public class GlobalResponseHandler extends ResponseBodyResultHandler {
    private static MethodParameter param;

    static {
        try {
            //get new params
            param = new MethodParameter(GlobalResponseHandler.class
                    .getDeclaredMethod("methodForParams"), -1);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public GlobalResponseHandler(List<HttpMessageWriter<?>> writers, RequestedContentTypeResolver resolver) {
        super(writers, resolver);
    }

    private static Mono<ServerResponse> methodForParams() {
        return null;
    }

    @Override
    public boolean supports(HandlerResult result) {
        boolean isMono = result.getReturnType().resolve() == Mono.class;
        boolean isAlreadyResponse = result.getReturnType().resolveGeneric(0) == ServerResponse.class;
        return isMono && !isAlreadyResponse;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Void> handleResult(ServerWebExchange exchange, HandlerResult result) {
        Preconditions.checkNotNull(result.getReturnValue(), "response is null!");
        // modify the result as you want
        Mono<RespBean> body = ((Mono<Object>) result.getReturnValue()).map(this::beforeBodyWrite).defaultIfEmpty(beforeBodyWrite(null));
        return writeBody(body, param, exchange);
    }

    public RespBean beforeBodyWrite(Object o) {
        if (o instanceof RespBean
//                || o instanceof FileSystemResource // 不包装文件流
//                || o instanceof Mono
//                || o instanceof Flux
//                || path.contains("swagger")
//                || path.equals("/error")
//                || path.equals("/v2/api-docs")
        ) {
            return (RespBean)o; // 防止多余的封装
        }
        ZLogger.newInstance().info(Optional.ofNullable(AroundController.UUIDS.get()).map(e -> e + ":").orElse("") + "Response Body : " + JsonUtil.obj2Json(o));
        return new RespBean<>(true, o, null, "成功", null, null);
    }
}