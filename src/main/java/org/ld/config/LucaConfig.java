package org.ld.config;

import com.github.jasync.r2dbc.mysql.JasyncConnectionFactory;
import com.github.jasync.sql.db.mysql.pool.MySQLConnectionFactory;
import com.github.jasync.sql.db.mysql.util.URLParser;
import org.ld.enums.ResponseMessageEnum;
import org.ld.utils.ServiceExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

//import javax.servlet.MultipartConfigElement;

/**
 * 获取配置中心的自定义配置信息
 *
 * @author ld
 */
@SuppressWarnings("unused")
@Configuration
//@EnableSwagger2
@EnableOpenApi
public class LucaConfig {

    /**
     * webflux 已没有这个东西
     * https://blog.csdn.net/lyboy0414/article/details/100763644
     * 增大默认上传文件大小的限制
     */
//    @Bean
//    public MultipartConfigElement multipartConfigElement() {
//        MultipartConfigFactory factory = new MultipartConfigFactory();
//        factory.setMaxFileSize(DataSize.ofMegabytes(2L));
//        factory.setMaxRequestSize(DataSize.ofMegabytes(10L));
//        return factory.createMultipartConfig();
//    }

    /**
     * https://blog.csdn.net/lilinhai548/article/details/107394670
     * http://127.0.0.1:9999/swagger-ui.html#/
     * http://127.0.0.1:9999/swagger-ui/index.html
     */
    @Bean
    public Docket createRestApi() {
        final var responseMessages = ResponseMessageEnum.getMessages();
        return new Docket(DocumentationType.OAS_30)
                .pathMapping("/")
                .globalResponseMessage(RequestMethod.GET, responseMessages)
                .globalResponseMessage(RequestMethod.POST, responseMessages)
                .globalResponseMessage(RequestMethod.PUT, responseMessages)
                .globalResponseMessage(RequestMethod.DELETE, responseMessages)
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.ld.controller"))
                .paths(PathSelectors.any())
                .build()
                // 支持的通讯协议集合
                .protocols(Set.of("https", "http"))
                .securitySchemes(List.of(new ApiKey("BASE_TOKEN", "token", "pass")))
                // 授权信息全局应用
                .securityContexts(List.of(
                        SecurityContext
                                .builder()
                                .securityReferences(
                                        List.of(new SecurityReference(
                                                        "BASE_TOKEN",
                                                        new AuthorizationScope[]{
                                                                new AuthorizationScope("global", "")
                                                        }
                                                )
                                        )
                                )
                                .build()
                ))
                .apiInfo(new ApiInfoBuilder()
                        .title("LUCA")
                        .description("系统介绍")
                        .contact(new Contact("ld", "", "2297598383@qq.com"))
                        .version("1.0")
                        .build());
    }

    /**
     * https://www.cnblogs.com/javazhiyin/p/9851775.html
     * 在Spring 环境下优先使用 RestTemplate 来发送Http请求
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Spring 的方式获取线程池
     */
    @Bean
    public ForkJoinPool pool() {
        return ServiceExecutor.getInstance();
    }

    @Autowired
    ServerCodecConfigurer serverCodecConfigurer;
    @Autowired
    RequestedContentTypeResolver requestedContentTypeResolver;

    @Bean
    GlobalResponseHandler responseWrapper() {
        return new GlobalResponseHandler(
                serverCodecConfigurer.getWriters(),
                requestedContentTypeResolver
        );
    }

    @Bean
    GlobalRequestBodyHandler globalRequestBodyHandler() {
        return new GlobalRequestBodyHandler(serverCodecConfigurer.getReaders());
    }

    @Bean
    public DatabaseClient databaseClient() {
        var url = "jdbc:mysql://127.0.0.1/luca?user=root&password=12345678";
        var connectionFactory = new JasyncConnectionFactory(new MySQLConnectionFactory(URLParser.INSTANCE.parseOrDie(url, StandardCharsets.UTF_8)));
        return DatabaseClient.create(connectionFactory);
    }

}