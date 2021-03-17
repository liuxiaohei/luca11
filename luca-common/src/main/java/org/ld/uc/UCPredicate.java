package org.ld.uc;

import org.ld.exception.CodeStackException;

import java.io.Serializable;
import java.util.function.Predicate;

@FunctionalInterface
public interface UCPredicate<T> extends Serializable, Predicate<T> {

    boolean testWithUC(T t) throws Throwable;

    @Override
    default boolean test(T t) {
        try {
            return testWithUC(t);
        } catch (Throwable e) {
            throw CodeStackException.of(e);
        }
    }
}