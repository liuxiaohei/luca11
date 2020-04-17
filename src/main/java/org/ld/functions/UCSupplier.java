package org.ld.functions;

@FunctionalInterface
public interface UCSupplier<R> {
    R get() throws Throwable;
}
