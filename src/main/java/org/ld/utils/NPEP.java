package org.ld.utils;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * NullPointerExceptionPreventer
 * 防止空指针异常的工具
 */
@SuppressWarnings("unused")
public class NPEP {

    /**
     * 防止Filter 返回null的情况导致StreamApi Optional 等方法产生空指针异常
     */
    public static <T>  Predicate<T> null2False(Predicate<T> predicate) {
        return obj -> {
            try {
                return Optional.ofNullable(obj).map(predicate::test).orElse(false);
            } catch (NullPointerException e) {
                return false;
            }
        };
    }

    /**
     * 当 val 值存在的时候，执行传入的方法，否则不处理
     */
    public static <T> void nonNullDo(Consumer<T> c, T val) {
        Optional.ofNullable(val).ifPresent(c);
    }

    /**
     * 如果 list 不为空 则执行 c
     */
    public static <T> void nonNullDo(Consumer<List<T>> c, List<T> list) {
        Optional.ofNullable(list).filter(StringUtil::isNotEmpty).ifPresent(c);
    }

}
