package org.ld.uc;

import java.io.Serializable;

/**
 * 自己递归自己的lambda
 */
@FunctionalInterface
public interface SelfFunction<T> extends Serializable {
    T apply(SelfFunction<T> self, T n);
}
