package org.ld.utils;

import java.util.concurrent.ForkJoinPool;

public class ServiceExecutor {

    private static class ServiceExecutorHolder {
        private static final ForkJoinPool Executor = new ForkJoinPool(
                20,
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                null,
                false);

    }

    public static ForkJoinPool getInstance() {
        return ServiceExecutorHolder.Executor;
    }
}
