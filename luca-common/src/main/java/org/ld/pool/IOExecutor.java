package org.ld.pool;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import org.ld.exception.CodeStackException;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * 针对IO密集型会频繁被阻塞操作的执行器
 * ps 阻塞操作不可以使用Thread.sleep要使用Fiber.sleep
 * https://copyfuture.com/blogs-details/20190919134144944ja9yh78mrpfpaw5
 */
public class IOExecutor {

    private static class ServiceExecutorHolder {
        private static final Executor Executor = runnable ->
                new Fiber<Void>() {
                    @Override
                    protected Void run() {
                        runnable.run();
                        return null;
                    }
                }.start();
    }

    public static void sleep(long millis) throws InterruptedException {
        sleep(millis, e -> {
            throw new CodeStackException(e);
        });
    }

    public static void sleep(long millis, Consumer<Throwable> whenSuspend) throws InterruptedException {
        try {
            Fiber.sleep(millis);
        } catch (SuspendExecution suspendExecution) {
            whenSuspend.accept(suspendExecution);
        }
    }

    public static Executor getInstance() {
        return ServiceExecutorHolder.Executor;
    }
}
