package org.ld.uc;

import java.io.Serializable;

/**
 * 自己递归自己的lambda
 */
@FunctionalInterface
public interface SelfFunction<T, R> extends Serializable {

    R apply(SelfFunction<T, R> self, T n);

    default R runwith(T n) {
        return apply(this, n);
    }
}
