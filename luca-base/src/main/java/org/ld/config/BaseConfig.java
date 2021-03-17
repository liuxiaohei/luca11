package org.ld.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class BaseConfig {
    @Value("${dev.mode:127.0.0.1:2181}")
    private String zkConnectString;
}
