package org.ld.grpc.config;

import io.grpc.Server;
import io.grpc.services.HealthStatusManager;
import io.netty.channel.Channel;
import org.ld.grpc.client.LucaGrpcClient;
import org.ld.grpc.server.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConditionalOnClass({Server.class, NettyGrpcServerFactory.class})
public class GrpcServerAutoConfiguration {

    @Bean
    public GrpcServerProperties defaultGrpcServerProperties() {
        return new GrpcServerProperties();
    }

    @Bean
    public GlobalServerInterceptorRegistry globalServerInterceptorRegistry() {
        return new GlobalServerInterceptorRegistry();
    }

    @Bean
    public AnnotationGrpcServiceDiscoverer defaultGrpcServiceFinder() {
        return new AnnotationGrpcServiceDiscoverer();
    }

    @Bean
    public HealthStatusManager healthStatusManager() {
        return new HealthStatusManager();
    }

    /**
     * 构造gprc服务代理类
     *
     * @param properties 配置文件实体
     * @param discoverer grpc发现服务接口
     * @return NettyGrpcServerFactory gprc服务代理类
     */
    @ConditionalOnClass(Channel.class)
    @Bean
    public NettyGrpcServerFactory nettyGrpcServiceFactory(GrpcServerProperties properties, AnnotationGrpcServiceDiscoverer discoverer) {
        var factory = new NettyGrpcServerFactory(properties);
        for (var service : discoverer.findGrpcServices()) {
            factory.addService(service);
        }
        return factory;
    }

    @Bean
    public GrpcServerLifecycle grpcServerLifecycle(NettyGrpcServerFactory factory) {
        return new GrpcServerLifecycle(factory);
    }

    @Bean
    public LucaGrpcClient lucagrpc() {
        return new LucaGrpcClient();
    }

}
