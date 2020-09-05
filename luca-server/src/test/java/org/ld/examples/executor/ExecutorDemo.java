package org.ld.examples.executor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.ld.utils.ZLogger;

import java.util.concurrent.*;

public class ExecutorDemo {

    public static void main(String[] args) {
        var executorService = new ThreadPoolExecutor(
                4,
                4,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName("一个线程");
                    thread.setDaemon(true);
                    return thread;
                },
                (r, e) -> {
                    throw new RejectedExecutionException("Task " + r.toString() +
                            " rejected from " +
                            e.toString());
                });
        var cf = CompletableFuture.runAsync(() -> System.out.println("一个样例"), executorService);
        cf.join();

        var executorService1 = new ForkJoinPool();
        var cf1 = CompletableFuture.runAsync(() -> System.out.println("又一个样例"), executorService1);
        cf.join();

        var executorService2 = new ThreadPoolExecutor(
                4,
                4,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat("luca-thread-%d").build(),
                (r, e) -> {
                    throw new RejectedExecutionException("Task " + r.toString() +
                            " rejected from " +
                            e.toString());
                });
        var cf2 = CompletableFuture.runAsync(() -> ZLogger.newInstance().info("又又一个样例"), executorService2);
        cf2.join();
    }
}
