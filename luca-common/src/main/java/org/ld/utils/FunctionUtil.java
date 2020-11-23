package org.ld.utils;

import org.ld.exception.CodeStackException;
import org.ld.uc.UCFunction;
import org.ld.uc.UCRunnable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 */
@SuppressWarnings("unused")
public class FunctionUtil {

    public static <T, R> R applyWithUC(UCFunction<T, R> function, T t) {
        try {
            return function.apply(t);
        } catch (Throwable throwable) {
            throw new CodeStackException(throwable);
        }
    }

    public static void runWithUC(UCRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            throw new CodeStackException(throwable);
        }
    }

    /**
     *
     */
    public static <T> void whenNonNullDo(Consumer<T> c, T val) {
        Optional.ofNullable(val).ifPresent(c);
    }

    /**
     *
     */
    public static <T> void whenNonNullDo(Consumer<List<T>> c, List<T> list) {
        Optional.ofNullable(list)
                .filter(StringUtil::isNotEmpty)
                .ifPresent(c);
    }
}
