package org.ld.utils;

import org.ld.beans.PageData;

import java.util.ArrayList;
import java.util.List;

/**
 * 内存分页
 */
public class PagingUtil {
    public static <T> PageData<T> paging(List<T> list, int start, int size) {
        start = size * start;
        int end = start + size;
        List<T> result;
        if (start > list.size()) {
            result = new ArrayList<>();
        } else if (end > list.size()) {
            result = list.subList(start, list.size());
        } else {
            result = list.subList(start, end);
        }
        PageData<T> pageData = new PageData<>();
        pageData.setList(result);
        pageData.setCount(list.size());
        return pageData;
    }
}
