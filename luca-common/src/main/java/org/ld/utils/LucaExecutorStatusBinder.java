package org.ld.utils;

import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 可将多个LucaExecutor的状态绑定到一起
 */
public class LucaExecutorStatusBinder<T> {

    private final Consumer<Throwable> whenAnyFail;

    private final Runnable onStart;

    private final Runnable afterCancel;

    private final Runnable whenAllSuccess;

    ExecutorService pool;
    Consumer<T> onSubJobStart;
    Consumer<T> onSubJobSuccess;
    BiConsumer<T, Throwable> onSubJobFailed;
    Function<T, String> mdcKey;
    Function<T, String> mdcValue;

    LucaExecutor changeStatusFunc(T object) {
        return new LucaExecutor(
                pool,
                () -> {
                    synchronized (object) {
                        try {
                            // todo
                            onStart.run();
                            onSubJobStart.accept(object);
                        } catch (Throwable e) {
                            whenAnyFail.accept(e);
                        }
                    }
                },
                () -> {
                    synchronized (object) {
                        onSubJobSuccess.accept(object);
                        whenAllSuccess.run(); // todo 计数
                    }
                },
                t -> {
                    synchronized (object) {
                        try {
                            onSubJobFailed.accept(object, t);
                        } catch (Throwable e) {
                            whenAnyFail.accept(e);
                        }
                    }
                },
                mdcKey.apply(object),
                mdcValue.apply(object)) {
            @Override
            public synchronized void cancel() {
                synchronized (object) {
                    try {
                        super.cancel();
                    } finally {
                        // todo 计数
                        afterCancel.run();
                    }
                }
            }
        };
    }

    public LucaExecutorStatusBinder(
            Consumer<Throwable> whenAnyFail,
            Runnable onStart,
            Runnable afterCancel,
            Runnable whenAllSuccess,
            ExecutorService pool,
            Consumer<T> onSubJobStart,
            Consumer<T> onSubJobSuccess,
            BiConsumer<T, Throwable> onSubJobFailed,
            Function<T, String> mdcKey,
            Function<T, String> mdcValue) {
        this.whenAnyFail = whenAnyFail;
        this.onStart = onStart;
        this.afterCancel = afterCancel;
        this.whenAllSuccess = whenAllSuccess;
        this.pool = pool;
        this.onSubJobStart = onSubJobStart;
        this.onSubJobSuccess = onSubJobSuccess;
        this.onSubJobFailed = onSubJobFailed;
        this.mdcKey = mdcKey;
        this.mdcValue = mdcValue;
    }

    enum Phase {
        START,
        CANCEL,
        SUCCESS,
        FAILURE;

        public boolean isDone() {
            return this == SUCCESS || this == FAILURE;
        }
    }

}
