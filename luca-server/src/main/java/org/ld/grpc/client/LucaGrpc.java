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

    private static final String SERVICE_NAME = "luca";
    private Object target;
    private Method method;
    private String params;

    @Override
    public final ServerServiceDefinition bindService() {
        return ServerServiceDefinition
                .builder(ServiceDescriptorHolder.serviceDescriptor)
                .addMethod(
                        MethodDescriptorHolder.methodDescriptor,
                        asyncUnaryCall((req, responseObserver) -> {
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

    /**
     * 单例获取方法的输入输出的描述
     */
    private static class MethodDescriptorHolder {
        private static final MethodDescriptor<GrpcObject, GrpcObject> methodDescriptor =
                MethodDescriptor
                        .<GrpcObject, GrpcObject>newBuilder()
                        .setType(MethodDescriptor.MethodType.UNARY)
                        .setFullMethodName(SERVICE_NAME + "/sendMessage")
                        .setSampledToLocalTracing(true)
                        .setRequestMarshaller(ProtoUtils.marshaller(new GrpcObject()))
                        .setResponseMarshaller(ProtoUtils.marshaller(new GrpcObject()))
                        .build();
    }

    /**
     * 整体服务的描述
     */
    private static class ServiceDescriptorHolder {
        private static final ServiceDescriptor serviceDescriptor =
                ServiceDescriptor
                        .newBuilder(SERVICE_NAME)
                        .addMethod(MethodDescriptorHolder.methodDescriptor)
                        .build();
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
            return blockingUnaryCall(getChannel(), MethodDescriptorHolder.methodDescriptor, getCallOptions(), request);
        }
    }

}