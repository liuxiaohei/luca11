package org.ld.grpc;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ld.LucaApplication;
import org.ld.beans.GrpcServer;
import org.ld.grpc.grpc.LucaGrpcClient;
import org.ld.grpc.schedule.ScheduleJob;
import org.ld.grpc.server.GrpcServerProperties;
import org.ld.mapper.CursorMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Import(GrpcServer.class)
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {LucaApplication.class})
@Slf4j
public class GrpcTest {

    @Resource
    private GrpcServerProperties grpcProperties;

    @Resource
    private CursorMapper cursorMapper;

    @Test
    @Transactional
    @SneakyThrows
    public void test1() {
        @Cleanup var c = cursorMapper.scan(1);
        c.forEach(e -> log.info("测试流式查询" + e.toString()));
    }

    @Test
    public void test() {
        LucaGrpcClient.sendMessage(grpcProperties.getAddress(), grpcProperties.getPort(),
                ScheduleJob.builder()
                        .beanName("grpcServer")
                        .methodName("run")
                        .params("demo")
                        .id(1)
                        .build());
        var a = LucaGrpcClient.sendMessage(grpcProperties.getAddress(), grpcProperties.getPort(),
                ScheduleJob.builder()
                        .beanName("grpcServer")
                        .methodName("run")
                        .params("ceshi")
                        .id(2)
                        .build());
    }
}
