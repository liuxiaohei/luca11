package org.ld.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@SuppressWarnings("unused")
public class CoreConfig {

    // todo 研究可以做什么
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
