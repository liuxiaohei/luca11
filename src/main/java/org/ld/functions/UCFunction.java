package org.ld.functions;

@FunctionalInterface
public interface UCFunction<T, R> {
    R apply(T t) throws Throwable;
}
