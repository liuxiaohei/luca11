package org.ld.uc;

import lombok.SneakyThrows;

import java.io.Serializable;

@FunctionalInterface
public interface UCRunnable extends Serializable, Runnable {

    void runWithUC() throws Throwable;

    @SneakyThrows
    default void run() {
        runWithUC();
    }

    static Runnable as(UCRunnable runnable) {
        return runnable;
    }
}
