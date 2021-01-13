package org.ld.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import reactor.util.annotation.NonNull;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * 可中断执行之后触发回调函数
 * 可监测执行期间的状态 当执行期间启动报错会自动触发回调函数
 * 自动配置MDC 对于使用者来说释放的过程透明
 * 相同的Executor对象的执行都只会在同一个异步线程内执行 可保证异步的同时具有顺序性
 * 并且可以通过这个对象中的cancel方法外部取消；
 */
@Slf4j
public class LucaExecutor implements Executor {

    /**
     * 取消线程是阻塞式的要异步调用才会加快取消的过程
     */
    private static class CancelServiceExecutorHolder {
        private static final ExecutorService Executor = new ForkJoinPool(64);
    }

    private final ExecutorService pool;

    private volatile CompletableFuture<Void> current;

    private volatile Thread runner;

    private volatile boolean canceled = false;

    private volatile boolean failed = false;

    private final Runnable onStart;

    private final Consumer<Throwable> onFailed;

    private final List<Runnable> afterCancels;

    private final String mdcKey;

    private final String mdcValue;

    /**
     * 成功后要主动调用成功的回调函数
     */
    @Getter
    private final Runnable onSuccess;

    @SuppressWarnings("unused")
    public synchronized void addAfterCancel(Runnable afterCancel) {
        this.afterCancels.add(0, afterCancel);
    }

    public synchronized void cancel() {
        if (canceled) {
            return;
        }
        canceled = true;
        CancelServiceExecutorHolder.Executor.submit(() -> {
            if (null != current) {
                current.handle((e, t) -> {
                    if (t instanceof InterruptedException) {
                        if (runner != null)
                            runner.interrupt();
                    }
                    afterCancels.parallelStream().forEach(Runnable::run);
                    return null;
                });
                try {
                    MDC.put(mdcKey, mdcValue);
                    current.cancel(false);
                    if (runner != null)
                        runner.interrupt();
                } catch (Throwable e) {
                    log.error("取消失败:", e);
                } finally {
                    MDC.remove(mdcKey);
                }
            }
        });
    }

    public LucaExecutor(ExecutorService pool,
                        Runnable onStart,
                        Runnable onSuccess,
                        Consumer<Throwable> onFailed,
                        String mdcKey, String mdcValue) {
        this.mdcKey = mdcKey;
        this.mdcValue = mdcValue;
        this.pool = pool;
        this.onStart = onStart;
        this.onSuccess = onSuccess;
        this.onFailed = onFailed;
        this.afterCancels = new CopyOnWriteArrayList<>();
    }

    @Override
    public synchronized void execute(@NonNull Runnable command) {
        if (failed || canceled) {
            return;
        }
        Runnable runnable = () -> {
            try {
                MDC.put(mdcKey, mdcValue);
                command.run();
            } catch (Throwable e) {
                failed = true;
                onFailed.accept(e);
                throw e;
            } finally {
                MDC.remove(mdcKey);
            }
        };
        current = null == current ? CompletableFuture.runAsync(() -> {
            try {
                MDC.put(mdcKey, mdcValue);
                runner = Thread.currentThread();
                onStart.run();
            } finally {
                MDC.remove(mdcKey);
            }
            runnable.run();
        }, pool) : current.thenRun(runnable);
    }
}
