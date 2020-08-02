package org.ld.config;

import org.ld.beans.RespBean;
import org.ld.enums.SystemErrorCodeEnum;
import org.ld.exception.CodeStackException;
import org.ld.exception.ErrorCode;
import org.ld.utils.ZLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 异常处理策略
 */
@Component
public class GlobalExceptionHandler implements WebExceptionHandler {

    private static final org.slf4j.Logger LOG = ZLogger.newInstance();

    // TODO 加jwt https://www.cnblogs.com/gdjlc/p/12081701.html
    public RespBean<Object> exceptionHandler(Throwable e) {
        LOG.error(AroundController.UUIDS.get(), e);
        final var se = Optional.of(e)
                .map(t -> {
                    var t1 = t;
                    while (null != t1) {
                        if (t1 instanceof CodeStackException) return (CodeStackException) t1;//找到第一个CodeException
                        t1 = t1.getCause();
                    }
                    return null;
                })
                .orElseGet(() -> new CodeStackException(e));
        final var result = new RespBean<>();
        result.setErrorCode(Optional.of(se)
                .map(CodeStackException::getErrorCode)
                .map(ErrorCode::getCode)
                .orElseGet(SystemErrorCodeEnum.UNKNOWN::getCode));
        result.setStackTrace(Optional.of(e)
                .map(error -> {
                    final var sw = new StringWriter();
                    final var pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    var strings = Stream.of(sw.toString().split("\n\t"))
                            .skip(1)
                            .map(str -> str.replace("\n", ""))
                            .toArray(String[]::new);
                    pw.flush();
                    return strings;
                }).orElse(null));
        result.setErrorMsgDescription(Optional.of(se)
                .map(CodeStackException::getErrorCode)
                .filter(i -> !i.getCode().equals(SystemErrorCodeEnum.UNKNOWN.getCode()))
                .map(ErrorCode::getMessage)
                .orElseGet(e::getMessage));
        result.setMessage(e.getMessage());
        result.setSuccess(false);
        return result;
    }

    /**
     * Handle the given exception. A completion signal through the return value
     * indicates error handling is complete while an error signal indicates the
     * exception is still not handled.
     *
     * @param exchange the current exchange
     * @param ex       the exception to handle
     * @return {@code Mono<Void>} to indicate when exception handling is complete
     */
    @Override
    public Mono<Void> handle(final ServerWebExchange exchange, final Throwable ex) {
        return handle(ex)
                .flatMap(it -> it.writeTo(exchange, new HandlerStrategiesResponseContext(HandlerStrategies.withDefaults())))
                .flatMap(i -> Mono.empty());
    }


    private Mono<ServerResponse> handle(Throwable ex) {
        return createResponse(HttpStatus.INTERNAL_SERVER_ERROR,   ex);
    }

    private Mono<ServerResponse> createResponse(final HttpStatus httpStatus, Throwable ex) {
        exceptionHandler(ex);
        return ServerResponse.status(httpStatus).syncBody(exceptionHandler(ex));
    }


    /**
     * The type Handler strategies response context.
     */
    static class HandlerStrategiesResponseContext implements ServerResponse.Context {

        private HandlerStrategies handlerStrategies;

        /**
         * Instantiates a new Handler strategies response context.
         *
         * @param handlerStrategies the handler strategies
         */
        public HandlerStrategiesResponseContext(final HandlerStrategies handlerStrategies) {
            this.handlerStrategies = handlerStrategies;
        }

        /**
         * Return the {@link HttpMessageWriter}s to be used for response body conversion.
         *
         * @return the list of message writers
         */
        @Override
        public List<HttpMessageWriter<?>> messageWriters() {
            return this.handlerStrategies.messageWriters();
        }

        /**
         * Return the  {@link ViewResolver}s to be used for view name resolution.
         *
         * @return the list of view resolvers
         */
        @Override
        public List<ViewResolver> viewResolvers() {
            return this.handlerStrategies.viewResolvers();
        }
    }
}
