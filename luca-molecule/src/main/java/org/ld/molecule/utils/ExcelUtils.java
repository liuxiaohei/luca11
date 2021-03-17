package org.ld.molecule.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * java 读取excel 的工具类
 * https://github.com/alibaba/easyexcel/blob/master/src/test/java/com/alibaba/easyexcel/test/demo/read/ReadTest.java
 */
@SuppressWarnings("unused")
public class ExcelUtils {

    public static <T> AnalysisEventListener<T> getListener(Consumer<List<T>> consumer, int threshold) {

        return new AnalysisEventListener<>() {
            private final LinkedList<T> linkedList = new LinkedList<>();

            @Override
            public void invoke(T t, AnalysisContext analysisContext) {
                linkedList.add(t);
                if (linkedList.size() >= threshold) {
                    consumer.accept(linkedList);
                    linkedList.clear();
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                if (linkedList.size() > 0) {
                    consumer.accept(linkedList);
                }
            }
        };
    }

    public static <T> AnalysisEventListener<T> getListener(Consumer<List<T>> consumer) {
        return getListener(consumer, 10);
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <T> List<T> readFile(Callable<InputStream> inputStreamSupplier, Class<T> tClass) {
        final List<T> dbStructures = new ArrayList<>();
        final List<Object> objects = new ArrayList<>();
        try (InputStream in = inputStreamSupplier.call()) {
            EasyExcel.read(in, tClass, getListener(objects::addAll))
                    .sheet()
                    .doRead();
        }
        objects.forEach(o -> dbStructures.add((T) o));
        return dbStructures;
    }
}
