package org.ld.pool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 *
 */
public class IOBlockExecutorService {

    private static class ServiceExecutorHolder {
        private static final ExecutorService Executor = new ThreadPoolExecutor(
                100,
                100,
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
    }

    public static ExecutorService getInstance() {
        return ServiceExecutorHolder.Executor;
    }
}
