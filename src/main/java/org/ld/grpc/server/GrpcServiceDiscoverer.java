package org.ld.grpc.server;


import java.util.Collection;


public interface GrpcServiceDiscoverer {
    Collection<GrpcServiceDefinition> findGrpcServices();
}
