package org.ld.functions;

@FunctionalInterface
public interface UCConsumer<T> {
    void accept(T t) throws Throwable;
}
