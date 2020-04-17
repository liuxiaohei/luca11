package org.ld.utils;

import org.ld.exception.CodeStackException;
import org.ld.functions.*;


import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 用于lambda表达式屏蔽受检异常
 *
 * @author ld
 */
@SuppressWarnings("unused")
public class Try {

    /**
     * 屏蔽受检异常
     */
    public static <T, R> Function<T, R> of(UCFunction<T, R> mapper) {
        Objects.requireNonNull(mapper);
        return t -> {
            try {
                return mapper.apply(t);
            } catch (Throwable ex) {
                throw new CodeStackException(ex);
            }
        };
    }

    /**
     * 屏蔽受检异常 并给出默认值默认值
     */
    public static <T, R> Function<T, R> of(UCFunction<T, R> mapper, Supplier<R> defaultValue) {
        Objects.requireNonNull(mapper);
        return t -> {
            try {
                return mapper.apply(t);
            } catch (Throwable ex) {
                return Optional.ofNullable(defaultValue).map(Supplier::get).orElse(null);
            }
        };
    }

    public static <T> Supplier<T> of(UCSupplier<T> uncheckedSupplier) {
        Objects.requireNonNull(uncheckedSupplier);
        return () -> {
            try {
                return uncheckedSupplier.get();
            } catch (Throwable ex) {
                throw new CodeStackException(ex);
            }
        };
    }

    public static <T> Supplier<T> of(UCSupplier<T> uncheckedSupplier, Supplier<T> defaultValue) {
        Objects.requireNonNull(uncheckedSupplier);
        return () -> {
            try {
                return uncheckedSupplier.get();
            } catch (Throwable ex) {
                return Optional.ofNullable(defaultValue).map(Supplier::get).orElse(null);
            }
        };
    }

    public static <T> Predicate<T> of(UCPredicate<T> uncheckedPredicate) {
        Objects.requireNonNull(uncheckedPredicate);
        return t -> {
            try {
                return uncheckedPredicate.test(t);
            } catch (Throwable ex) {
                throw new CodeStackException(ex);
            }
        };
    }

    public static <T> Predicate<T> of(UCPredicate<T> uncheckedPredicate, boolean defaultValue) {
        Objects.requireNonNull(uncheckedPredicate);
        return t -> {
            try {
                return uncheckedPredicate.test(t);
            } catch (Throwable ex) {
                return defaultValue;
            }
        };
    }

    public static <T> Consumer<T> of(UCConsumer<T> uncheckedConsumer) {
        Objects.requireNonNull(uncheckedConsumer);
        return t -> {
            try {
                uncheckedConsumer.accept(t);
            } catch (Throwable ex) {
                throw new CodeStackException(ex);
            }
        };
    }

    public static Runnable of(UCRunnable uncheckedRunnable) {
        Objects.requireNonNull(uncheckedRunnable);
        return () -> {
            try {
                uncheckedRunnable.run();
            } catch (Throwable ex) {
                throw new CodeStackException(ex);
            }
        };
    }

}