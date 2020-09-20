package org.ld.grpc;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ld.LucaApplication;
import org.ld.beans.GrpcServer;
import org.ld.grpc.client.LucaGrpc;
import org.ld.grpc.schedule.ScheduleJob;
import org.ld.grpc.server.GrpcServerProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Import(GrpcServer.class)
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {LucaApplication.class})
@Slf4j
public class GrpcTest {

    @Resource
    private GrpcServerProperties grpcProperties;

    @Test
    public void test() {
        ScheduleJob beanTest = new ScheduleJob();
        beanTest.setBeanName("grpcServer");
        beanTest.setMethodName("run");
        beanTest.setId(1);
        LucaGrpc.sendMessage(grpcProperties.getAddress(), grpcProperties.getPort(), beanTest);
    }
}
