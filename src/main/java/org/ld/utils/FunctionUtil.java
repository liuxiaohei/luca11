package org.ld.utils;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 *
 */
@SuppressWarnings("unused")
public class FunctionUtil {

    /**
     */
    public static <T> void whenNonNullDo(Consumer<T> c, T val) {
        Optional.ofNullable(val).ifPresent(c);
    }

    /**
     */
    public static <T> void whenNonNullDo(Consumer<List<T>> c, List<T> list) {
        Optional.ofNullable(list).filter(StringUtil::isNotEmpty).ifPresent(c);
    }
}
