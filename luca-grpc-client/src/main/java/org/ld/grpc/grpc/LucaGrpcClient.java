package org.ld.grpc.grpc;

import io.grpc.*;
import io.grpc.protobuf.ProtoUtils;
import lombok.extern.slf4j.Slf4j;
import org.ld.grpc.schedule.ScheduleJob;
import org.ld.grpc.server.GrpcService;
import org.ld.pool.IOExecutor;
import org.ld.utils.SpringBeanFactory;
import org.ld.utils.StringUtil;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;

@GrpcService(LucaGrpcClient.class)
@Slf4j
public final class LucaGrpcClient implements BindableService {

    private static final String SERVICE_NAME = "luca";

    /**
     * https://www.imooc.com/article/38147
     * https://blog.csdn.net/zhu_tianwei/article/details/44065097
     * 接收端反射调用的核心逻辑
     */
    @Override
    public final ServerServiceDefinition bindService() {
        return ServerServiceDefinition
                .builder(ServiceDescriptorHolder.serviceDescriptor)
                .addMethod(
                        MethodDescriptorHolder.methodDescriptor,
                        asyncUnaryCall((req, responseObserver) -> {
                            // grpc服务端接受消息方法
                            // 通过反射调用具体需要执行方法
                            var scheduleJob = req.getObj(ScheduleJob.class);
                            var message = "SUCCESS";
                            try {
                                var target = SpringBeanFactory.getBean(scheduleJob.getBeanName());
                                var params = scheduleJob.getParams();
                                Method method;
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
                                }, IOExecutor.getInstance());
                            } catch (Exception e) {
                                message = "UNKNOWN";
                                log.error("任务执行失败:{}，任务id:{}", message, scheduleJob.getId());
                            }
                            responseObserver.onNext(GrpcMessage.stringObj(message));
                            responseObserver.onCompleted();
                        }))
                .build();
    }

    /**
     * 单例获取方法的输入输出的描述
     */
    private static class MethodDescriptorHolder {
        private static final MethodDescriptor<GrpcMessage, GrpcMessage> methodDescriptor =
                MethodDescriptor
                        .<GrpcMessage, GrpcMessage>newBuilder()
                        .setType(MethodDescriptor.MethodType.UNARY)
                        .setFullMethodName(SERVICE_NAME + "/sendMessage")
                        .setSampledToLocalTracing(true)
                        .setRequestMarshaller(ProtoUtils.marshaller(new GrpcMessage()))
                        .setResponseMarshaller(ProtoUtils.marshaller(new GrpcMessage()))
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

    /**
     * 向远程发送一个请求
     */
    public static String sendMessage(String ip, int grpcPort, ScheduleJob req) {
        return sendMessage(ip,grpcPort,GrpcMessage.obj(req)).getStringObj();
    }

    /**
     * 向远程发送一个请求
     */
    private static GrpcMessage sendMessage(String ip, int grpcPort, GrpcMessage request) {
        ManagedChannel channel = null;
        try {
            channel = ManagedChannelBuilder.forAddress(ip, grpcPort)
                    .usePlaintext()
                    .build();
            var grpcReply = blockingUnaryCall(channel, MethodDescriptorHolder.methodDescriptor, CallOptions.DEFAULT, request);
            if ("UNKNOWN".equals(grpcReply.getStringObj())) {
                var scheduleJob = request.getObj(ScheduleJob.class);
                log.error("任务执行失败:{}，任务id:{}", grpcReply.getStringObj(), scheduleJob.getId());
            }
            if ("SUCCESS".equals(grpcReply.getStringObj())) {
                var scheduleJob = request.getObj(ScheduleJob.class);
                log.info("任务执行成功:{}，任务id:{}", grpcReply.getStringObj(), scheduleJob.getId());
            }
            return grpcReply;
        } catch (Exception e) {
            log.error("grpc启动异常:{}", e.getMessage());
            throw e;
        } finally {
            try {
                if (channel != null) {
                    channel.shutdown().awaitTermination(500, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
                log.error("", e1);
            }
        }
    }
}