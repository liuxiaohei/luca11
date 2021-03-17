package org.ld.uc;

import org.ld.exception.CodeStackException;

import java.io.Serializable;
import java.util.function.Supplier;

@FunctionalInterface
public interface UCSupplier<T> extends Serializable, Supplier<T> {
    
    T getWithUC() throws Throwable;

    default T get() {
        try {
            return getWithUC();
        } catch (Throwable e) {
            throw CodeStackException.of(e);
        }
    }
}