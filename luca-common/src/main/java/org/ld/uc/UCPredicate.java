package org.ld.uc;

import java.io.Serializable;

@FunctionalInterface
public interface UCPredicate<T> extends Serializable {
    boolean test(T t) throws Throwable;
}