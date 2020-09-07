package org.ld.uc;

import java.io.Serializable;

@FunctionalInterface
public interface UCFunction<T, R> extends Serializable {
    R apply(T t) throws Throwable;
}