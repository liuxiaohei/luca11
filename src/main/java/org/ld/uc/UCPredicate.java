package org.ld.uc;

@FunctionalInterface
public interface UCPredicate<T> {
    boolean test(T t) throws Throwable;
}
