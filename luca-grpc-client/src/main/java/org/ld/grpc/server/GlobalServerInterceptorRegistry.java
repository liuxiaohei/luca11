package org.ld.grpc.server;

import com.google.common.collect.Lists;
import com.netflix.appinfo.ApplicationInfoManager;
import io.grpc.ServerInterceptor;
import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.SocketUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Getter
public class GlobalServerInterceptorRegistry {

    private final List<ServerInterceptor> serverInterceptors = Lists.newArrayList();
    @Resource
    private GrpcServerProperties grpcProperties;
    @Resource
    private ApplicationInfoManager instance;

    /**
     * 初始化方法、注册监听ip、端口
     */
    @PostConstruct
    public void init() {
        var port = SocketUtils.findAvailableTcpPort();
        grpcProperties.setPort(port);
        grpcProperties.setAddress(instance.getEurekaInstanceConfig().getIpAddress());
        //将grpc监听端口放入注册中心
        instance.getInfo().getMetadata().put("grpcPort", String.valueOf(port));
    }

    public GlobalServerInterceptorRegistry addServerInterceptors(ServerInterceptor interceptor) {
        serverInterceptors.add(interceptor);
        return this;
    }
}