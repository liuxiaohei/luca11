package org.ld.pool;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * 针对CPU密集型操作的执行器 性能更好但是线程数有限怕阻塞
 */
public class CPUExecutor {

    private static class ServiceExecutorHolder {
        private static final java.util.concurrent.Executor Executor = new ForkJoinPool(
                5,
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                null,
                false);
    }

    public static Executor getInstance() {
        return ServiceExecutorHolder.Executor;
    }
}
