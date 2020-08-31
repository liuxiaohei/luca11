package org.ld.grpc.server;

import io.grpc.Server;


public interface GrpcServerFactory {
    Server createServer();

    String getAddress();

    int getPort();

    void addService(GrpcServiceDefinition service);

    void destroy();
}
