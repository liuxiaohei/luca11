package org.ld.grpc.client;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.ServiceDescriptor;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;

import static io.grpc.stub.ClientCalls.blockingUnaryCall;

public final class LucaGrpc {

    public static final String SERVICE_NAME = "luca";
    private static volatile MethodDescriptor<GrpcRequest, GrpcReply> getSendMessageMethod;
    private static volatile ServiceDescriptor serviceDescriptor;

    public static MethodDescriptor<GrpcRequest, GrpcReply> getSendMessageMethod() {
        MethodDescriptor<GrpcRequest, GrpcReply> getSendMessageMethod;
        if ((getSendMessageMethod = LucaGrpc.getSendMessageMethod) == null) {
            synchronized (LucaGrpc.class) {
                if ((getSendMessageMethod = LucaGrpc.getSendMessageMethod) == null) {
                    LucaGrpc.getSendMessageMethod
                            = getSendMessageMethod
                            = MethodDescriptor
                            .<GrpcRequest, GrpcReply>newBuilder()
                            .setType(MethodDescriptor.MethodType.UNARY)
                            .setFullMethodName("luca/sendMessage")
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
            synchronized (LucaGrpc.class) {
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

    static final class MethodHandlers<T, R> implements
            ServerCalls.UnaryMethod<T, R>,
            ServerCalls.ServerStreamingMethod<T, R>,
            ServerCalls.ClientStreamingMethod<T, R>,
            ServerCalls.BidiStreamingMethod<T, R> {

        private final LucaGrpcImpl serviceImpl;
        private final int methodId;

        MethodHandlers(LucaGrpcImpl serviceImpl, int methodId) {
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