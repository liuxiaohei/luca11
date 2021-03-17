package org.ld.uc;

import lombok.SneakyThrows;

import java.io.Serializable;
import java.util.function.Predicate;

@FunctionalInterface
public interface UCPredicate<T> extends Serializable, Predicate<T> {

    boolean testWithUC(T t) throws Throwable;

    @Override
    @SneakyThrows
    default boolean test(T t) {
        return testWithUC(t);
    }
}