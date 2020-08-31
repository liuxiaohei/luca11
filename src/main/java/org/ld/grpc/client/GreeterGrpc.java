package org.ld.grpc.client;

import com.google.protobuf.Descriptors;
import io.grpc.*;
import io.grpc.protobuf.ProtoFileDescriptorSupplier;
import io.grpc.protobuf.ProtoMethodDescriptorSupplier;
import io.grpc.protobuf.ProtoServiceDescriptorSupplier;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

public final class GreeterGrpc {

    public static final String SERVICE_NAME = "Greeter";
    private static final int METHODID_SEND_MESSAGE = 0;
    private static volatile MethodDescriptor<GrpcRequest,
            GrpcReply> getSendMessageMethod;
    private static volatile ServiceDescriptor serviceDescriptor;

    private GreeterGrpc() {
    }

    public static MethodDescriptor<GrpcRequest,
            GrpcReply> getSendMessageMethod() {
        MethodDescriptor<GrpcRequest, GrpcReply> getSendMessageMethod;
        if ((getSendMessageMethod = GreeterGrpc.getSendMessageMethod) == null) {
            synchronized (GreeterGrpc.class) {
                if ((getSendMessageMethod = GreeterGrpc.getSendMessageMethod) == null) {
                    GreeterGrpc.getSendMessageMethod = getSendMessageMethod =
                            MethodDescriptor.<GrpcRequest, GrpcReply>newBuilder()
                                    .setType(MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(
                                            "Greeter", "sendMessage"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(ProtoUtils.marshaller(
                                            GrpcRequest.getDefaultInstance()))
                                    .setResponseMarshaller(ProtoUtils.marshaller(
                                            GrpcReply.getDefaultInstance()))
                                    .setSchemaDescriptor(new GreeterMethodDescriptorSupplier("sendMessage"))
                                    .build();
                }
            }
        }
        return getSendMessageMethod;
    }

    public static GreeterBlockingStub newBlockingStub(
            Channel channel) {
        return new GreeterBlockingStub(channel);
    }

    public static ServiceDescriptor getServiceDescriptor() {
        ServiceDescriptor result = serviceDescriptor;
        if (result == null) {
            synchronized (GreeterGrpc.class) {
                result = serviceDescriptor;
                if (result == null) {
                    serviceDescriptor = result = ServiceDescriptor.newBuilder(SERVICE_NAME)
                            .setSchemaDescriptor(new GreeterFileDescriptorSupplier())
                            .addMethod(getSendMessageMethod())
                            .build();
                }
            }
        }
        return result;
    }

    public static abstract class GreeterImplBase implements BindableService {

        public void sendMessage(GrpcRequest request,
                                StreamObserver<GrpcReply> responseObserver) {
            asyncUnimplementedUnaryCall(getSendMessageMethod(), responseObserver);
        }

        @Override
        public final ServerServiceDefinition bindService() {
            return ServerServiceDefinition.builder(getServiceDescriptor())
                    .addMethod(
                            getSendMessageMethod(),
                            asyncUnaryCall(
                                    new MethodHandlers<>(
                                            this, METHODID_SEND_MESSAGE)))
                    .build();
        }
    }

    public static final class GreeterBlockingStub extends AbstractStub<GreeterBlockingStub> {
        private GreeterBlockingStub(Channel channel) {
            super(channel);
        }

        private GreeterBlockingStub(Channel channel,
                                    CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected GreeterBlockingStub build(Channel channel,
                                            CallOptions callOptions) {
            return new GreeterBlockingStub(channel, callOptions);
        }

        public GrpcReply sendMessage(GrpcRequest request) {
            return blockingUnaryCall(
                    getChannel(), getSendMessageMethod(), getCallOptions(), request);
        }
    }

    private static final class MethodHandlers<Req, Resp> implements
            ServerCalls.UnaryMethod<Req, Resp>,
            ServerCalls.ServerStreamingMethod<Req, Resp>,
            ServerCalls.ClientStreamingMethod<Req, Resp>,
            ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final GreeterImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(GreeterImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void invoke(Req request, StreamObserver<Resp> responseObserver) {
            if (methodId == METHODID_SEND_MESSAGE) {
                serviceImpl.sendMessage((GrpcRequest) request,
                        (StreamObserver<GrpcReply>) responseObserver);
            } else {
                throw new AssertionError();
            }
        }

        @Override
        public StreamObserver<Req> invoke(
                StreamObserver<Resp> responseObserver) {
            throw new AssertionError();
        }
    }

    private static abstract class GreeterBaseDescriptorSupplier
            implements ProtoFileDescriptorSupplier, ProtoServiceDescriptorSupplier {
        GreeterBaseDescriptorSupplier() {
        }

        @Override
        public Descriptors.FileDescriptor getFileDescriptor() {
            return GrpcProto.getDescriptor();
        }

        @Override
        public Descriptors.ServiceDescriptor getServiceDescriptor() {
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
            implements ProtoMethodDescriptorSupplier {
        private final String methodName;

        GreeterMethodDescriptorSupplier(String methodName) {
            this.methodName = methodName;
        }

        @Override
        public Descriptors.MethodDescriptor getMethodDescriptor() {
            return getServiceDescriptor().findMethodByName(methodName);
        }
    }
}
