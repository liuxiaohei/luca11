package org.ld.grpc.client;

import io.grpc.*;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;

import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

public final class GreeterGrpc {

    public static final String SERVICE_NAME = "Greeter";
    private static volatile MethodDescriptor<GrpcRequest, GrpcReply> getSendMessageMethod;
    private static volatile ServiceDescriptor serviceDescriptor;

    public static MethodDescriptor<GrpcRequest, GrpcReply> getSendMessageMethod() {
        MethodDescriptor<GrpcRequest, GrpcReply> getSendMessageMethod;
        if ((getSendMessageMethod = GreeterGrpc.getSendMessageMethod) == null) {
            synchronized (GreeterGrpc.class) {
                if ((getSendMessageMethod = GreeterGrpc.getSendMessageMethod) == null) {
                    GreeterGrpc.getSendMessageMethod
                            = getSendMessageMethod
                            = MethodDescriptor
                            .<GrpcRequest, GrpcReply>newBuilder()
                            .setType(MethodDescriptor.MethodType.UNARY)
                            .setFullMethodName("Greeter/sendMessage")
                            .setSampledToLocalTracing(true)
                            .setRequestMarshaller(ProtoUtils.marshaller(new GrpcRequest()))
                            .setResponseMarshaller(ProtoUtils.marshaller(new GrpcReply()))
                            .build();
                }
            }
        }
        return getSendMessageMethod;
    }

    public static ServiceDescriptor getServiceDescriptor() {
        ServiceDescriptor result = serviceDescriptor;
        if (result == null) {
            synchronized (GreeterGrpc.class) {
                result = serviceDescriptor;
                if (result == null) {
                    serviceDescriptor
                            = result
                            = ServiceDescriptor.newBuilder(SERVICE_NAME)
                            .addMethod(getSendMessageMethod())
                            .build();
                }
            }
        }
        return result;
    }

    public static class GreeterImplBase implements BindableService {

        public void sendMessage(GrpcRequest request, StreamObserver<GrpcReply> responseObserver) {
            asyncUnimplementedUnaryCall(getSendMessageMethod(), responseObserver);
        }

        @Override
        public final ServerServiceDefinition bindService() {
            return ServerServiceDefinition.builder(getServiceDescriptor())
                    .addMethod(getSendMessageMethod(), asyncUnaryCall(new MethodHandlers<>(this, 0)))
                    .build();
        }
    }

    public static final class GreeterBlockingStub extends AbstractStub<GreeterBlockingStub> {

        public GreeterBlockingStub(Channel channel) {
            super(channel);
        }

        private GreeterBlockingStub(Channel channel, CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected GreeterBlockingStub build(Channel channel, CallOptions callOptions) {
            return new GreeterBlockingStub(channel, callOptions);
        }

        public GrpcReply sendMessage(GrpcRequest request) {
            return blockingUnaryCall(getChannel(), getSendMessageMethod(), getCallOptions(), request);
        }
    }

    private static final class MethodHandlers<T, R> implements
            ServerCalls.UnaryMethod<T, R>,
            ServerCalls.ServerStreamingMethod<T, R>,
            ServerCalls.ClientStreamingMethod<T, R>,
            ServerCalls.BidiStreamingMethod<T, R> {

        private final GreeterImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(GreeterImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void invoke(T request, StreamObserver<R> responseObserver) {
            if (methodId == 0) {
                serviceImpl.sendMessage((GrpcRequest) request, (StreamObserver<GrpcReply>) responseObserver);
            } else {
                throw new AssertionError();
            }
        }

        @Override
        public StreamObserver<T> invoke(StreamObserver<R> responseObserver) {
            throw new AssertionError();
        }
    }

}