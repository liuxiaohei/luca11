package org.ld.grpc.server;

import io.grpc.Server;
import lombok.extern.slf4j.Slf4j;
import org.ld.exception.CodeStackException;
import org.springframework.context.SmartLifecycle;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class GrpcServerLifecycle implements SmartLifecycle {
    private static final AtomicInteger serverCounter = new AtomicInteger(-1);
    private final org.ld.grpc.server.NettyGrpcServerFactory factory;
    private volatile Server server;

    public GrpcServerLifecycle(org.ld.grpc.server.NettyGrpcServerFactory factory) {
        this.factory = factory;
    }

    /**
     * 启动grpc服务
     */
    @Override
    public void start() {
        try {
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
        } catch (IOException e) {
            throw new CodeStackException(e);
        }
    }

    @Override
    public void stop() {
        factory.destroy();
        if (this.server != null) {
            this.server.shutdown();
            this.server = null;
            log.debug("gRPC server shutdown.");
        }
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

}
