package org.ld.config;

import akka.actor.ActorSystem;
import lombok.extern.log4j.Log4j2;
import org.flywaydb.core.Flyway;
import org.ld.annotation.NeedToken;
import org.ld.beans.RespBean;
import org.ld.enums.ResponseMessageEnum;
import org.ld.enums.UserErrorCodeEnum;
import org.ld.exception.CodeStackException;
import org.ld.utils.JsonUtil;
import org.ld.utils.JwtUtils;
import org.ld.utils.ServiceExecutor;
import org.ld.utils.SnowflakeId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.result.method.annotation.AbstractMessageReaderArgumentResolver;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.sql.DriverManager;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

/**
 * 获取配置中心的自定义配置信息
 *
 * @author ld
 */
@Configuration
@EnableOpenApi
@Log4j2
public class LucaConfig {

    @Resource
    private ServerCodecConfigurer serverCodecConfigurer;

    @Resource
    private RequestedContentTypeResolver requestedContentTypeResolver;

    @Resource
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Resource
    private DataSourceConfig dataSourceConfig;

    /**
     * https://blog.csdn.net/lilinhai548/article/details/107394670
     * http://127.0.0.1:9999/swagger-ui.html#/
     * http://127.0.0.1:9999/swagger-ui/index.html
     */
    @Bean
    Docket openApi() {
        final var responseMessages = ResponseMessageEnum.getMessages();
        return new Docket(DocumentationType.OAS_30)
                .pathMapping("/")
                .globalResponses(HttpMethod.GET, responseMessages)
                .globalResponses(HttpMethod.POST, responseMessages)
                .globalResponses(HttpMethod.PUT, responseMessages)
                .globalResponses(HttpMethod.DELETE, responseMessages)
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.ld.controller"))
                .paths(PathSelectors.any())
                .build()
                .protocols(Set.of("https", "http"))
                .securitySchemes(List.of(new ApiKey(HttpHeaders.AUTHORIZATION, HttpHeaders.AUTHORIZATION, "header")))
                .securityContexts(List.of(
                        SecurityContext.builder()
                                .securityReferences(List.of(
                                        new SecurityReference(
                                                HttpHeaders.AUTHORIZATION,
                                                new AuthorizationScope[]{
                                                        new AuthorizationScope("global", "accessEverything")
                                                })
                                ))
                                .build()
                ))
                .apiInfo(new ApiInfoBuilder()
                        .title("LUCA")
                        .description("系统介绍")
                        .contact(new Contact("ld", "www.baidu.com", "2297598383@qq.com"))
                        .version("1.0")
                        .build());
    }

    /**
     * https://www.cnblogs.com/javazhiyin/p/9851775.html
     * 在Spring 环境下优先使用 RestTemplate 来发送Http请求
     */
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Spring 的方式获取线程池
     */
    @Bean
    ForkJoinPool pool() {
        return ServiceExecutor.getInstance();
    }

    //https://blog.csdn.net/lizz861109/article/details/106303929
    @Bean
    ResponseBodyResultHandler responseBodyHandler() {
        return new ResponseBodyResultHandler(
                serverCodecConfigurer.getWriters(),
                requestedContentTypeResolver
        ) {
            @Override
            public boolean supports(@NotNull HandlerResult result) {
                var isMono = result.getReturnType().resolve() == Mono.class;
                var isAlreadyResponse = result.getReturnType().resolveGeneric(0) == ServerResponse.class;
                return isMono && !isAlreadyResponse;
            }

            @NotNull
            @Override
            @SuppressWarnings("unchecked")
            public Mono<Void> handleResult(@NotNull ServerWebExchange exchange, @NotNull HandlerResult result) {
                Objects.requireNonNull(result.getReturnValue(), "response is null!");
                var body = ((Mono<Object>) result.getReturnValue())
                        .map(o -> {
                            if (o instanceof RespBean) {
                                return (RespBean<Object>) o; // 防止多余的封装
                            }
                            final var snowflakeId = Optional.ofNullable(AroundController.UUIDS.get())
                                    .orElseGet(() -> SnowflakeId.get().toString());
                            AroundController.UUIDS.set(snowflakeId);
                            log.info(Optional.ofNullable(AroundController.UUIDS.get())
                                    .map(e -> e + ":")
                                    .orElse("") + "Response Body : " + JsonUtil.obj2Json(o));
                            return new RespBean<>(o);
                        });
                return writeBody(body, MethodParameterHolder.param, exchange);
            }
        };
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    /**
     * 请求体策略
     */
    @Bean
    AbstractMessageReaderArgumentResolver requestBodyHandler() {
        return new AbstractMessageReaderArgumentResolver(serverCodecConfigurer.getReaders()) {
            @Override
            public boolean supportsParameter(@NotNull MethodParameter methodParameter) {
                return true;
            }

            @NotNull
            @Override
            public Mono<Object> resolveArgument(@NotNull MethodParameter param, @NotNull BindingContext bindingContext, @NotNull ServerWebExchange exchange) {
                var ann = param.getParameterAnnotation(RequestBody.class);
                final var shortUUid = Optional.ofNullable(AroundController.UUIDS.get()).orElseGet(() -> SnowflakeId.get().toString());
                log.info(shortUUid + ":RequestBody : {}", JsonUtil.obj2Json(ann));
                assert ann != null;
                return readBody(param, ann.required(), bindingContext, exchange);
            }
        };
    }

    @Bean
    GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @RestControllerAdvice
    static class GlobalExceptionHandler {
        @ExceptionHandler(Exception.class)
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        public RespBean exceptionHandler(Throwable e) {
            log.error(AroundController.UUIDS.get(), e);
            return new RespBean(e);
        }
    }

    /**
     * 拦截器
     */
    @Bean
    WebFilter webFilter() {
        return (exchange, chain) -> {
            var handlerMethod = requestMappingHandlerMapping.getHandler(exchange).toProcessor().peek();
            //注意跨域时的配置，跨域时浏览器会先发送一个option请求，这时候getHandler不会时真正的HandlerMethod
            if (handlerMethod instanceof HandlerMethod) {
                var loginToken = ((HandlerMethod) handlerMethod).getMethodAnnotation(NeedToken.class);
                var request = exchange.getRequest();
                if (loginToken != null && loginToken.required()) {
                    var tokenHeader = request.getHeaders().get(JwtUtils.TOKEN_HEADER);
                    if (tokenHeader == null || tokenHeader.size() == 0) {
                        throw UserErrorCodeEnum.EMPTY_TOKEN.getException();
                    }
                    var token = tokenHeader.get(0).replace(JwtUtils.TOKEN_PREFIX, "");
                    JwtUtils.verify(token);
                }
            }
            return chain.filter(exchange);
        };
    }

    @Bean
    public ActorSystem actorSystem() {
        return ActorSystemHolder.ACTORSYSTEM;
    }

    @PostConstruct
    @SuppressWarnings("all")
    public void init() {
        final var targetDb = Optional.ofNullable(dataSourceConfig.getJdbcUrl())
                .map(str -> str.split("/"))
                .map(s -> s[s.length - 1])
                .map(str -> str.split("[?]"))
                .map(strs -> strs[0]).orElse("");
        final var rawJdbcUrl = Optional.ofNullable(dataSourceConfig.getJdbcUrl())
                .map(e -> e.split(targetDb)[0])
                .filter(e -> !e.contains("useSSL")).map(e -> e + "?useSSL=false")
                .orElse("");
        final var initSql = "CREATE DATABASE IF NOT EXISTS " + targetDb + " CHARACTER SET utf8";
        final var flyway = Flyway
                .configure()
                .dataSource(
                        Optional.of(dataSourceConfig.getJdbcUrl())
                                .filter(e -> !e.contains("useSSL")).map(e -> e + "&useSSL=false")
                                .orElse(dataSourceConfig.getJdbcUrl())
                        , dataSourceConfig.getUserName()
                        , dataSourceConfig.getPassWord()).load();
        log.info("init Db jdbcUrl:" + rawJdbcUrl + " database:" + targetDb); //自动创建database
        try (final var connection = DriverManager.getConnection(
                rawJdbcUrl,
                dataSourceConfig.getUserName(),
                dataSourceConfig.getPassWord());
             final var statement = connection.createStatement()
        ) {
            statement.execute(initSql);
            flyway.repair();
            flyway.migrate();
        } catch (Exception e) {
            log.error("", e);
            throw new CodeStackException(e);
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static class ActorSystemHolder {
        public static final ActorSystem ACTORSYSTEM = ActorSystem.create("lucaSystem");
    }

    public static class MethodParameterHolder {

        private static Mono<ServerResponse> methodForParams() {
            return null;
        }

        static final MethodParameter param = new MethodParameter(
                Optional.of(MethodParameterHolder.class).map(e -> {
                    try {
                        return e.getDeclaredMethod("methodForParams");
                    } catch (NoSuchMethodException noSuchMethodException) {
                        throw new CodeStackException(noSuchMethodException);
                    }
                }).get(), -1);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}