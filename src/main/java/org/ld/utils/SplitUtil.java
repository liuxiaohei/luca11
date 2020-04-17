package org.ld.utils;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class SplitUtil {

    /**
     * 将List按指定长度分裂成最长长度为len的Lists
     */
    public static <T> List<List<T>> splitList(List<T> list, final int len) {
        if (list == null || list.size() == 0) {
            return null;
        }
        List<List<T>> result = new ArrayList<>();
        int size = list.size();
        int count = (size + len - 1) / len;
        for (int i = 0; i < count; i++) {
            List<T> subList = list.subList(i * len, Math.min((i + 1) * len, size));
            result.add(subList);
        }
        return result;
    }
}
