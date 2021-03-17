package org.ld.uc;

import org.ld.exception.CodeStackException;

import java.io.Serializable;

@FunctionalInterface
public interface UCRunnable extends Serializable, Runnable {

    void runWithUC() throws Throwable;

    default void run() {
        try {
            runWithUC();
        } catch (Throwable e) {
            throw CodeStackException.of(e);
        }
    }

    static Runnable as(UCRunnable runnable) {
        return runnable;
    }
}
