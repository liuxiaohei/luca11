package org.ld.pool;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 针对IO密集型会频繁被阻塞操作的执行器
 * ps 阻塞操作不可以使用Thread.sleep要使用Fiber.sleep
 * https://copyfuture.com/blogs-details/20190919134144944ja9yh78mrpfpaw5
 */
public class IOExecutor {

    private static class ServiceExecutorHolder {
        private static final Executor Executor = CompletableFuture::runAsync;
    }

    public static Executor getInstance() {
        return ServiceExecutorHolder.Executor;
    }
}
