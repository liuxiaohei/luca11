package org.ld.uc;

import java.io.Serializable;

@FunctionalInterface
public interface UCSupplier<R> extends Serializable {
    R get() throws Throwable;
}