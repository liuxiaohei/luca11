package org.ld.grpc.client;

import io.grpc.*;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.AbstractStub;
import lombok.extern.slf4j.Slf4j;
import org.ld.grpc.schedule.ScheduleJob;
import org.ld.grpc.server.GrpcService;
import org.ld.utils.JsonUtil;
import org.ld.utils.SpringBeanFactory;
import org.ld.utils.StringUtil;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;

@GrpcService(LucaGrpc.class)
@Slf4j
public final class LucaGrpc implements BindableService {

    public static final String SERVICE_NAME = "luca";
    private static volatile MethodDescriptor<GrpcObject, GrpcObject> getSendMessageMethod;
    private static volatile ServiceDescriptor serviceDescriptor;
    private Object target;
    private Method method;
    private String params;

    @Override
    public final ServerServiceDefinition bindService() {
        return ServerServiceDefinition
                .builder(getServiceDescriptor())
                .addMethod(
                        getSendMessageMethod(),
                        asyncUnaryCall((req,responseObserver) -> {
                            // grpc服务端接受消息方法
                            // 通过反射调用具体需要执行方法
                            String name = req.getValue();
                            ScheduleJob scheduleJob = JsonUtil.json2Obj(name, ScheduleJob.class);
                            String message = "SUCCESS";
                            try {
                                target = SpringBeanFactory.getBean(scheduleJob.getBeanName());
                                params = scheduleJob.getParams();
                                if (StringUtil.isBlank(params)) {
                                    method = target.getClass().getDeclaredMethod(scheduleJob.getMethodName());
                                } else {
                                    method = target.getClass().getDeclaredMethod(scheduleJob.getMethodName(), String.class);
                                }
                                ReflectionUtils.makeAccessible(method);
                                CompletableFuture.runAsync(() -> {
                                    try {
                                        if (StringUtil.isBlank(params)) {
                                            method.invoke(target);
                                        } else {
                                            method.invoke(target, params);
                                        }
                                    } catch (Exception e) {
                                        log.error("执行定时任务失败:{},任务id:{}", e, scheduleJob.getId());
                                    }
                                });
                            } catch (Exception e) {
                                message = "UNKNOWN";
                                log.error("任务执行失败:{}，任务id:{}", message, scheduleJob.getId());
                            }
                            responseObserver.onNext(new GrpcObject(message));
                            responseObserver.onCompleted();
                        }))
                .build();
    }

    private static MethodDescriptor<GrpcObject, GrpcObject> getSendMessageMethod() {
        MethodDescriptor<GrpcObject, GrpcObject> getSendMessageMethod;
        if ((getSendMessageMethod = LucaGrpc.getSendMessageMethod) == null) {
            synchronized (LucaGrpc.class) {
                if ((getSendMessageMethod = LucaGrpc.getSendMessageMethod) == null) {
                    LucaGrpc.getSendMessageMethod
                            = getSendMessageMethod
                            = MethodDescriptor
                            .<GrpcObject, GrpcObject>newBuilder()
                            .setType(MethodDescriptor.MethodType.UNARY)
                            .setFullMethodName("luca/sendMessage")
                            .setSampledToLocalTracing(true)
                            .setRequestMarshaller(ProtoUtils.marshaller(new GrpcObject()))
                            .setResponseMarshaller(ProtoUtils.marshaller(new GrpcObject()))
                            .build();
                }
            }
        }
        return getSendMessageMethod;
    }

    private static ServiceDescriptor getServiceDescriptor() {
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

        public GrpcObject sendMessage(GrpcObject request) {
            return blockingUnaryCall(getChannel(), getSendMessageMethod(), getCallOptions(), request);
        }
    }

}