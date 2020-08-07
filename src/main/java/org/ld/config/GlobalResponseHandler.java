package org.ld.config;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.ld.beans.OnSuccessResp;
import org.ld.utils.JsonUtil;
import org.ld.utils.SnowflakeId;
import org.ld.utils.ZLogger;
import org.springframework.core.MethodParameter;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

//https://xbuba.com/questions/48705172
//https://blog.csdn.net/lizz861109/article/details/106303929
public class GlobalResponseHandler extends ResponseBodyResultHandler {
    private static MethodParameter param;

    static {
        try {
            param = new MethodParameter(GlobalResponseHandler.class.getDeclaredMethod("methodForParams"), -1);
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
        var isMono = result.getReturnType().resolve() == Mono.class;
        var isAlreadyResponse = result.getReturnType().resolveGeneric(0) == ServerResponse.class;
        return isMono && !isAlreadyResponse;
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public Mono<Void> handleResult(@NotNull ServerWebExchange exchange, HandlerResult result) {
        Preconditions.checkNotNull(result.getReturnValue(), "response is null!");
        var body = ((Mono<Object>) result.getReturnValue())
                .map(o -> {
                    if (o instanceof OnSuccessResp) {
                        return (OnSuccessResp<Object>) o; // 防止多余的封装
                    }
                    final var snowflakeId = Optional.ofNullable(AroundController.UUIDS.get())
                            .orElseGet(() -> SnowflakeId.get().toString());
                    AroundController.UUIDS.set(snowflakeId);
                    ZLogger.newInstance()
                            .info(Optional.ofNullable(AroundController.UUIDS.get())
                                    .map(e -> e + ":")
                                    .orElse("") + "Response Body : " + JsonUtil.obj2Json(o));
                    return new OnSuccessResp<>(o);
                });
        return writeBody(body, param, exchange);
    }

}