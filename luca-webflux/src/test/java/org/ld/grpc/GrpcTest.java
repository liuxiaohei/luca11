package org.ld.grpc;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ld.LucaApplication;
import org.ld.mapper.CursorMapper;
import org.ld.schedule.client.ScheduleClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {LucaApplication.class})
@Slf4j
public class GrpcTest {

    @Resource
    private CursorMapper cursorMapper;

    @Resource
    private ScheduleClient scheduleClient;

    @Test
    @Transactional
    @SneakyThrows
    public void test1() {
        @Cleanup var c = cursorMapper.scan(1);
        c.forEach(e -> log.info("测试流式查询" + e.toString()));
    }

    @Test
    public void test() {
//        scheduleClient.send(
//                ScheduleJob.builder()
//                        .beanName("grpcServer")
//                        .methodName("run")
//                        .params("demo")
//                        .id(1)
//                        .build());
//        scheduleClient.send(
//                ScheduleJob.builder()
//                        .beanName("grpcServer")
//                        .methodName("run")
//                        .params("ceshi")
//                        .id(2)
//                        .build());
    }
}
