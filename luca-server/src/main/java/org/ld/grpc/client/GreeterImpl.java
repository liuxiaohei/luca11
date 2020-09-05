package org.ld.grpc.client;

import io.grpc.stub.StreamObserver;
import org.ld.grpc.schedule.ScheduleJob;
import org.ld.grpc.server.GrpcService;
import org.ld.utils.JsonUtil;
import org.ld.utils.SpringBeanFactory;
import org.ld.utils.StringUtil;
import org.ld.utils.ZLogger;
import org.slf4j.Logger;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

@GrpcService(GreeterGrpc.class)
public class GreeterImpl extends GreeterGrpc.GreeterImplBase {

    private Object target;
    private Method method;
    private String params;
    private final Logger log = ZLogger.newInstance();

    /**
     * grpc服务端接受消息方法
     * 通过反射调用具体需要执行方法
     * @param req              传入参数
     * @param responseObserver 返回结果
     */
    @Override
    public void sendMessage(GrpcRequest req, StreamObserver<GrpcReply> responseObserver) {
        String name = req.getParams();
        ScheduleJob scheduleJob = JsonUtil.json2Obj(name, ScheduleJob.class);
        String message = "SUCCESS";
        try {
            assert scheduleJob != null;
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
        responseObserver.onNext(GrpcReply.newBuilder().setMessage((message)).build());
        responseObserver.onCompleted();
    }
}
