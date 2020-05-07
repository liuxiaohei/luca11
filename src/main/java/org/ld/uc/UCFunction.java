package org.ld.uc;

@FunctionalInterface
public interface UCFunction<T, R> {
    R apply(T t) throws Throwable;
}
