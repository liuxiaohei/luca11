package org.ld.schedule.config;

import org.ld.schedule.client.ScheduleClient;
import org.ld.schedule.server.ScheduleServer;
import org.ld.schedule.endpoint.GreeterHttpController;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

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

    // eureka
//    @Resource
//    private ApplicationInfoManager instance;

    @Resource
    private ZookeeperDiscoveryProperties instance;

    public static final String METADATA_LABEL = "label";

    /**
     * 初始化方法、注册监听ip、端口
     */
    @PostConstruct
    public void init() {
//        var port = SocketUtils.findAvailableTcpPort();
        // eureka方式
//        instance.getInfo().getMetadata().put("grpcPort", String.valueOf(port));
        // zk 方式
        instance.getMetadata().put(METADATA_LABEL, "master");
    }

}
