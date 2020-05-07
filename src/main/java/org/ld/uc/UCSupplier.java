package org.ld.uc;

@FunctionalInterface
public interface UCSupplier<R> {
    R get() throws Throwable;
}
