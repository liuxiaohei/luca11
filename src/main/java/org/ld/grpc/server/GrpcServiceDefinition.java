package org.ld.grpc.server;

import io.grpc.ServerServiceDefinition;

public class GrpcServiceDefinition {
    private final String beanName;
    private final Class<?> beanClazz;
    private final ServerServiceDefinition definition;

    public GrpcServiceDefinition(String beanName, Class<?> beanClazz,
                                 ServerServiceDefinition definition) {
        super();
        this.beanName = beanName;
        this.beanClazz = beanClazz;
        this.definition = definition;
    }

    public String getBeanName() {
        return this.beanName;
    }

    public Class<?> getBeanClazz() {
        return this.beanClazz;
    }

    public ServerServiceDefinition getDefinition() {
        return this.definition;
    }
}
