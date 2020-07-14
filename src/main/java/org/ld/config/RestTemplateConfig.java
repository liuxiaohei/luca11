package org.ld.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * https://www.cnblogs.com/javazhiyin/p/9851775.html
 * 在Spring 环境下优先使用 RestTemplate 来发送Http请求
 */
@Configuration
@SuppressWarnings("unused")
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
