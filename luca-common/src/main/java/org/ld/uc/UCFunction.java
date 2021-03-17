package org.ld.uc;

import org.ld.exception.CodeStackException;
import org.springframework.cglib.core.internal.Function;

import java.io.Serializable;

@FunctionalInterface
public interface UCFunction<T, R> extends Serializable, Function<T,R> {

    R applyWithUC(T t) throws Throwable;

    default R apply(T t) {
        try {
            return applyWithUC(t);
        } catch (Throwable e) {
            throw CodeStackException.of(e);
        }
    }
}