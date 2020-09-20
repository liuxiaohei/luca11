package org.ld.grpc.server;

import io.grpc.Server;
import org.ld.utils.ZLogger;
import org.slf4j.Logger;
import org.springframework.context.SmartLifecycle;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;


public class GrpcServerLifecycle implements SmartLifecycle {
    private static final AtomicInteger serverCounter = new AtomicInteger(-1);
    private final NettyGrpcServerFactory factory;
    private volatile Server server;
    private final Logger log = ZLogger.newInstance();

    public GrpcServerLifecycle(NettyGrpcServerFactory factory) {
        this.factory = factory;
    }

    /**
     * 启动grpc服务
     */
    @Override
    public void start() {
        try {
            createAndStartGrpcServer();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void stop() {
        stopAndReleaseGrpcServer();
    }

    @Override
    public void stop(Runnable callback) {
        this.stop();
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return this.server != null && !this.server.isShutdown();
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    /**
     * 运行grpc服务实例
     */
    protected void createAndStartGrpcServer() throws IOException {
        Server localServer = this.server;
        if (localServer == null) {
            this.server = this.factory.createServer();
            this.server.start();
            log.debug("gRPC Server started, listening on address: " + this.factory.getAddress() + ", port: " + this.factory.getPort());

            Thread awaitThread = new Thread("container-" + (serverCounter.incrementAndGet())) {
                @Override
                public void run() {
                    try {
                        GrpcServerLifecycle.this.server.awaitTermination();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

            };
            awaitThread.setDaemon(false);
            awaitThread.start();
        }
    }

    /**
     * 停止并关闭服务
     */
    protected void stopAndReleaseGrpcServer() {
        factory.destroy();
        Server localServer = this.server;
        if (localServer != null) {
            localServer.shutdown();
            this.server = null;
            log.debug("gRPC server shutdown.");
        }
    }

}
