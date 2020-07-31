package org.ld.uc;

import java.io.Serializable;

/**
 * Y 组合字
 * https://www.dazhuanlan.com/2019/11/14/5dccfcb2adb63/
 */
@FunctionalInterface
public interface YCombinator<T, R> extends Serializable {

    R apply(YCombinator<T, R> self, T n);

    default R apply(T n) {
        return apply(this, n);
    }
}
