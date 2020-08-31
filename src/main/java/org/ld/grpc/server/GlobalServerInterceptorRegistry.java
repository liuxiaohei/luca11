package org.ld.grpc.server;

import com.google.common.collect.Lists;
import com.netflix.appinfo.ApplicationInfoManager;
import io.grpc.ServerInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.SocketUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

public class GlobalServerInterceptorRegistry implements ApplicationContextAware {

    private final List<ServerInterceptor> serverInterceptors = Lists.newArrayList();
    private ApplicationContext applicationContext;
    @Autowired
    private GrpcServerProperties grpcProperties;
    @Autowired
    private ApplicationInfoManager instance;

    /**
     * 初始化方法、注册监听ip、端口
     */
    @PostConstruct
    public void init() {
        int port = SocketUtils.findAvailableTcpPort();
        grpcProperties.setPort(port);
        grpcProperties.setAddress(instance.getEurekaInstanceConfig().getIpAddress());
        //将grpc监听端口放入注册中心
        instance.getInfo().getMetadata().put("grpcPort", String.valueOf(port));
        Map<String, GlobalServerInterceptorConfigurerAdapter> map = applicationContext.getBeansOfType(GlobalServerInterceptorConfigurerAdapter.class);
        for (GlobalServerInterceptorConfigurerAdapter globalServerInterceptorConfigurerAdapter : map.values()) {
            globalServerInterceptorConfigurerAdapter.addServerInterceptors(this);
        }
    }

    public GlobalServerInterceptorRegistry addServerInterceptors(ServerInterceptor interceptor) {
        serverInterceptors.add(interceptor);
        return this;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public List<ServerInterceptor> getServerInterceptors() {
        return serverInterceptors;
    }
}