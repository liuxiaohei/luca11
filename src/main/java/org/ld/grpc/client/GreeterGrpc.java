package org.ld.grpc.client;

import io.grpc.stub.StreamObserver;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

public final class GreeterGrpc {

    public static final String SERVICE_NAME = "Greeter";
    private static final int METHODID_SEND_MESSAGE = 0;
    private static volatile io.grpc.MethodDescriptor<GrpcRequest,
            GrpcReply> getSendMessageMethod;
    private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

    private GreeterGrpc() {
    }

    public static io.grpc.MethodDescriptor<GrpcRequest,
            GrpcReply> getSendMessageMethod() {
        io.grpc.MethodDescriptor<GrpcRequest, GrpcReply> getSendMessageMethod;
        if ((getSendMessageMethod = GreeterGrpc.getSendMessageMethod) == null) {
            synchronized (GreeterGrpc.class) {
                if ((getSendMessageMethod = GreeterGrpc.getSendMessageMethod) == null) {
                    GreeterGrpc.getSendMessageMethod = getSendMessageMethod =
                            io.grpc.MethodDescriptor.<GrpcRequest, GrpcReply>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(
                                            "Greeter", "sendMessage"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            GrpcRequest.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            GrpcReply.getDefaultInstance()))
                                    .setSchemaDescriptor(new GreeterMethodDescriptorSupplier("sendMessage"))
                                    .build();
                }
            }
        }
        return getSendMessageMethod;
    }

    public static GreeterBlockingStub newBlockingStub(
            io.grpc.Channel channel) {
        return new GreeterBlockingStub(channel);
    }

    public static io.grpc.ServiceDescriptor getServiceDescriptor() {
        io.grpc.ServiceDescriptor result = serviceDescriptor;
        if (result == null) {
            synchronized (GreeterGrpc.class) {
                result = serviceDescriptor;
                if (result == null) {
                    serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
                            .setSchemaDescriptor(new GreeterFileDescriptorSupplier())
                            .addMethod(getSendMessageMethod())
                            .build();
                }
            }
        }
        return result;
    }

    public static abstract class GreeterImplBase implements io.grpc.BindableService {

        public void sendMessage(GrpcRequest request,
                                io.grpc.stub.StreamObserver<GrpcReply> responseObserver) {
            asyncUnimplementedUnaryCall(getSendMessageMethod(), responseObserver);
        }

        @Override
        public final io.grpc.ServerServiceDefinition bindService() {
            return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                    .addMethod(
                            getSendMessageMethod(),
                            asyncUnaryCall(
                                    new MethodHandlers<>(
                                            this, METHODID_SEND_MESSAGE)))
                    .build();
        }
    }

    public static final class GreeterBlockingStub extends io.grpc.stub.AbstractStub<GreeterBlockingStub> {
        private GreeterBlockingStub(io.grpc.Channel channel) {
            super(channel);
        }

        private GreeterBlockingStub(io.grpc.Channel channel,
                                    io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected GreeterBlockingStub build(io.grpc.Channel channel,
                                            io.grpc.CallOptions callOptions) {
            return new GreeterBlockingStub(channel, callOptions);
        }

        public GrpcReply sendMessage(GrpcRequest request) {
            return blockingUnaryCall(
                    getChannel(), getSendMessageMethod(), getCallOptions(), request);
        }
    }

    private static final class MethodHandlers<Req, Resp> implements
            io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final GreeterImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(GreeterImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            if (methodId == METHODID_SEND_MESSAGE) {
                serviceImpl.sendMessage((GrpcRequest) request,
                        (StreamObserver<GrpcReply>) responseObserver);
            } else {
                throw new AssertionError();
            }
        }

        @Override
        public io.grpc.stub.StreamObserver<Req> invoke(
                io.grpc.stub.StreamObserver<Resp> responseObserver) {
            throw new AssertionError();
        }
    }

    private static abstract class GreeterBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
        GreeterBaseDescriptorSupplier() {
        }

        @Override
        public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
            return GrpcProto.getDescriptor();
        }

        @Override
        public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
            return getFileDescriptor().findServiceByName("Greeter");
        }
    }

    private static final class GreeterFileDescriptorSupplier
            extends GreeterBaseDescriptorSupplier {
        GreeterFileDescriptorSupplier() {
        }
    }

    private static final class GreeterMethodDescriptorSupplier
            extends GreeterBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
        private final String methodName;

        GreeterMethodDescriptorSupplier(String methodName) {
            this.methodName = methodName;
        }

        @Override
        public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
            return getServiceDescriptor().findMethodByName(methodName);
        }
    }
}
