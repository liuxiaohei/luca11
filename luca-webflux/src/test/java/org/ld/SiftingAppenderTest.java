package org.ld;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Slf4j
public class SiftingAppenderTest {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService taskExecutors = Executors.newCachedThreadPool();
        // 运行10个task，启动了10个线程
        IntStream.rangeClosed(1, 10).forEach(i ->
                CompletableFuture.runAsync(() -> {
                    try {
                        MDC.put("LogUuid", i + "");
                        log.info("taskId={}, threadNo={}", i, Thread.currentThread());
                    } finally {
                        MDC.remove(i + "");
                    }

                }));
        Thread.sleep(1000L);
        taskExecutors.shutdown();


    }
}
