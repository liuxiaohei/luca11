package org.ld.pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 *
 */
public class CPUFJExecutorService {

    private static class ServiceExecutorHolder {
        private static final ExecutorService Executor = new ForkJoinPool(
                5,
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                null,
                false);
    }

    public static ExecutorService getInstance() {
        return ServiceExecutorHolder.Executor;
    }
}
