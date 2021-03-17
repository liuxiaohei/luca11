package org.ld.uc;

import lombok.SneakyThrows;

import java.io.Serializable;
import java.util.function.Supplier;

@FunctionalInterface
public interface UCSupplier<T> extends Serializable, Supplier<T> {

    T getWithUC() throws Throwable;

    @SneakyThrows
    default T get() {
        return getWithUC();
    }
}