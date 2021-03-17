package org.ld.uc;

import org.ld.exception.CodeStackException;

import java.io.Serializable;
import java.util.function.Consumer;

@FunctionalInterface
public interface UCConsumer<T> extends Serializable, Consumer<T> {

    void acceptWithUC(T t) throws Throwable;

    default void accept(T t) {
        try {
            acceptWithUC(t);
        } catch (Throwable e) {
            throw CodeStackException.of(e);
        }
    }
}