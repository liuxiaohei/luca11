package org.ld.grpc.server;

import com.google.common.collect.Lists;
import io.grpc.ServerInterceptor;
import lombok.Getter;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.util.SocketUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

//import com.netflix.appinfo.ApplicationInfoManager;

@Getter
public class GlobalServerInterceptorRegistry {

    private final List<ServerInterceptor> serverInterceptors = Lists.newArrayList();
    @Resource
    private GrpcServerProperties grpcProperties;


    // eureka
//    @Resource
//    private ApplicationInfoManager instance;

    @Resource
    private ZookeeperDiscoveryProperties instance;

    /**
     * 初始化方法、注册监听ip、端口
     */
    @PostConstruct
    public void init() {
        var port = SocketUtils.findAvailableTcpPort();
        grpcProperties.setPort(port);
        // eureka方式
//        grpcProperties.setAddress(instance.getEurekaInstanceConfig().getIpAddress());
//        instance.getInfo().getMetadata().put("grpcPort", String.valueOf(port));
        // zk 方式
        grpcProperties.setAddress(instance.getInstanceHost());
        instance.getMetadata().put("grpcPort", String.valueOf(port));

    }

    public GlobalServerInterceptorRegistry addServerInterceptors(ServerInterceptor interceptor) {
        serverInterceptors.add(interceptor);
        return this;
    }
}