package org.ld.schedule.config;

import org.ld.schedule.client.ScheduleClient;
import org.ld.schedule.server.ScheduleServer;
import org.ld.schedule.endpoint.GreeterHttpController;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties
@Import(ScheduleClient.class)
public class ServerAutoConfiguration {

    @Bean
    public ScheduleServer scheduleServer() {
        return new ScheduleServer();
    }

    @Bean
    public GreeterHttpController greeterHttpController() {
        return new GreeterHttpController();
    }

}
