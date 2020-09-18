package org.ld.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * NullPointerExceptionPreventer
 * 防止空指针异常的工具
 */
@SuppressWarnings("unused")
public class NPEP {

    /**
     * 防止Filter 返回null的情况导致StreamApi Optional 等方法产生空指针异常
     */
    public static <T> Predicate<T> null2False(Predicate<T> predicate) {
        return obj -> {
            try {
                return Optional.ofNullable(obj).map(predicate::test).orElse(false);
            } catch (NullPointerException e) {
                return false;
            }
        };
    }

    public static <T> Stream<T> safeStream(List<T> list) {
        return Optional.ofNullable(list).orElseGet(ArrayList::new).stream();
    }

}
