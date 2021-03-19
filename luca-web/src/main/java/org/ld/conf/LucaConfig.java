package org.ld.conf;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 获取配置中心的自定义配置信息
 *
 * @author ld
 */
@Configuration
@EnableSwagger2
@Log4j2
public class LucaConfig {

    /**
     * https://blog.csdn.net/lilinhai548/article/details/107394670
     * http://127.0.0.1:9999/swagger-ui.html#/
     * http://127.0.0.1:9999/swagger-ui/index.html
     */
    @Bean
    public Docket api(){
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("luca")
                .version("1.0")
                .build();
    }
}