package org.ld.uc;

import java.io.Serializable;

@FunctionalInterface
public interface UCConsumer<T> extends Serializable {
    void accept(T t) throws Throwable;
}