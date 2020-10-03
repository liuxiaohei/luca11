package org.ld.pool;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;

import java.util.concurrent.Executor;

/**
 * 针对IO密集型会频繁被阻塞操作的执行器
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
        try {
            Fiber.sleep(millis);
        } catch (SuspendExecution suspendExecution) {
            suspendExecution.printStackTrace();
        }
    }

    public static Executor getInstance() {
        return ServiceExecutorHolder.Executor;
    }
}
