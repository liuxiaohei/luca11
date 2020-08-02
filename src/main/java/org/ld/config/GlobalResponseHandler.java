package org.ld.config;

import com.google.common.base.Preconditions;
import org.springframework.core.MethodParameter;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
//https://xbuba.com/questions/48705172
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
        Mono<ServerResponse> body = ((Mono<ServerResponse>) result.getReturnValue());
        return writeBody(body, param, exchange);
    }
}