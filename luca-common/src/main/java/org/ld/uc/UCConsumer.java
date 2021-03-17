package org.ld.uc;

import lombok.SneakyThrows;

import java.io.Serializable;
import java.util.function.Consumer;

@FunctionalInterface
public interface UCConsumer<T> extends Serializable, Consumer<T> {

    void acceptWithUC(T t) throws Throwable;

    @SneakyThrows
    default void accept(T t) {
        acceptWithUC(t);
    }
}