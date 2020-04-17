package org.ld.functions;

@FunctionalInterface
public interface UCPredicate<T> {
    boolean test(T t) throws Throwable;
}
