package org.ld.uc;

@FunctionalInterface
public interface UCConsumer<T> {
    void accept(T t) throws Throwable;
}
