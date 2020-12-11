package org.ld.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

/**
 * 类描述：自定义health
 */
@Configuration
public class MyHealthIndicator
        implements HealthIndicator {
    // http://localhost:8769/actuator/health
    @Override
    public Health health() {
        return Health.status(Status.UP)
                .withDetail("myName", "ld")
                .withDetail("timeStamp", Instant.now())
                .build();
    }
}

