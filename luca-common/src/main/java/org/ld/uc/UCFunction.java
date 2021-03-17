package org.ld.uc;

import lombok.SneakyThrows;
import org.springframework.cglib.core.internal.Function;

import java.io.Serializable;

@FunctionalInterface
public interface UCFunction<T, R> extends Serializable, Function<T, R> {

    R applyWithUC(T t) throws Throwable;

    @SneakyThrows
    default R apply(T t) {
        return applyWithUC(t);
    }
}