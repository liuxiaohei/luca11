package org.ld.pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 *
 */
public class IOBlockServiceExecutor {

    private static class ServiceExecutorHolder {
        private static final ExecutorService Executor = new ForkJoinPool(
                100,
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                null,
                false);

    }

    public static ExecutorService getInstance() {
        return ServiceExecutorHolder.Executor;
    }
}
