package org.ld.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ld.LucaApplication;
import org.ld.beans.GrpcBean;
import org.ld.beans.GrpcServer;
import org.ld.grpc.client.GreeterGrpc;
import org.ld.grpc.client.GrpcReply;
import org.ld.grpc.client.GrpcRequest;
import org.ld.grpc.server.GrpcServerProperties;
import org.ld.utils.JsonUtil;
import org.ld.utils.ZLogger;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Import(GrpcServer.class)
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {LucaApplication.class})
@Slf4j
public class GrpcTest {
    @Resource
    private GrpcServerProperties grpcProperties;

    @Test
    public void test() throws InterruptedException {
        GrpcBean beanTest = new GrpcBean();
        beanTest.beanName = "grpcServer";
        beanTest.methodName = "run";
        ManagedChannel channel = ManagedChannelBuilder.forAddress(grpcProperties.getAddress(), grpcProperties.getPort())
                .usePlaintext()
                .build();
        GreeterGrpc.GreeterBlockingStub blockingStub = GreeterGrpc.newBlockingStub(channel);
        GrpcRequest request = GrpcRequest.newBuilder().setParams(JsonUtil.obj2Json(beanTest)).build();
        try {
            GrpcReply grpcReply = blockingStub.sendMessage(request);
            log.info("grpc启动测试结果:{}", grpcReply.getMessage());
        } catch (Exception e) {
            log.error("grpc启动异常:{}", e.getMessage());
        } finally {
            channel.shutdown().awaitTermination(500, TimeUnit.SECONDS);
        }
    }
}
