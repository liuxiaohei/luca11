package org.ld.grpc.server;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import io.grpc.Server;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.services.HealthStatusManager;
import org.ld.utils.ZLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.List;


public class NettyGrpcServerFactory {

    private final GrpcServerProperties properties;
    private final List<GrpcServiceDefinition> services = Lists.newLinkedList();
    private Logger log = ZLogger.newInstance();
    @Autowired
    private HealthStatusManager healthStatusManager;

    public NettyGrpcServerFactory(GrpcServerProperties properties) {
        this.properties = properties;
    }

    /**
     * 获取grpc服务
     *
     * @return Server 注册进来的服务
     */
    public Server createServer() {
        NettyServerBuilder builder = NettyServerBuilder.forAddress(
                new InetSocketAddress(InetAddresses.forString(getAddress()), getPort()));
        // support health check
        builder.addService(healthStatusManager.getHealthService());
        for (GrpcServiceDefinition service : this.services) {
            String serviceName = service.getDefinition().getServiceDescriptor().getName();
            log.debug("Registered gRPC service: " + serviceName + ", bean: " + service.getBeanName() + ", class: " + service.getBeanClazz().getName());
            builder.addService(service.getDefinition());
            healthStatusManager.setStatus(serviceName, HealthCheckResponse.ServingStatus.SERVING);
        }

        if (this.properties.getSecurity().getEnabled()) {
            File certificateChain = new File(this.properties.getSecurity().getCertificateChainPath());
            File certificate = new File(this.properties.getSecurity().getCertificatePath());
            builder.useTransportSecurity(certificateChain, certificate);
        }
        if (properties.getMaxMessageSize() > 0) {
            builder.maxInboundMessageSize(properties.getMaxMessageSize());
        }

        return builder.build();
    }

    public String getAddress() {
        return this.properties.getAddress();
    }

    public int getPort() {
        return this.properties.getPort();
    }

    public void addService(GrpcServiceDefinition service) {
        this.services.add(service);
    }

    /**
     * 移除服务
     */
    public void destroy() {
        for (GrpcServiceDefinition grpcServiceDefinition : services) {
            String serviceName = grpcServiceDefinition.getDefinition().getServiceDescriptor().getName();
            healthStatusManager.clearStatus(serviceName);
        }
    }
}
