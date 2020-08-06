package org.ld.config;

import org.jetbrains.annotations.NotNull;
import org.ld.utils.JsonUtil;
import org.ld.utils.SnowflakeId;
import org.ld.utils.ZLogger;
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.annotation.AbstractMessageReaderArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

/**
 * 请求体策略
 */
public class GlobalRequestBodyHandler  extends AbstractMessageReaderArgumentResolver {

    private static final org.slf4j.Logger LOG = ZLogger.newInstance();

    protected GlobalRequestBodyHandler(List<HttpMessageReader<?>> readers) {
        super(readers);
    }

    protected GlobalRequestBodyHandler(List<HttpMessageReader<?>> messageReaders, ReactiveAdapterRegistry adapterRegistry) {
        super(messageReaders, adapterRegistry);
    }

    @Override
    public boolean supportsParameter(@NotNull MethodParameter methodParameter) {
        return true;
    }

    @NotNull
    @Override
    public Mono<Object> resolveArgument(MethodParameter param, @NotNull BindingContext bindingContext, @NotNull ServerWebExchange exchange) {
        var ann = param.getParameterAnnotation(RequestBody.class);
        final var shortUUid = Optional.ofNullable(AroundController.UUIDS.get()).orElseGet(() -> SnowflakeId.get().toString());
        LOG.info(shortUUid + ":RequestBody : {}", JsonUtil.obj2Json(ann));
        assert ann != null;
        return readBody(param, ann.required(), bindingContext, exchange);
    }
}
