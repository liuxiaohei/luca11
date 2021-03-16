package org.ld.utils;

import org.ld.exception.CodeStackException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * todo 加入自定义的实现
 */
@SuppressWarnings("unused")
public class ZLogger {

    private static final Map<String, org.slf4j.Logger> cache = new ConcurrentHashMap<>();

    public static org.slf4j.Logger newInstance() {
        return cache.computeIfAbsent(Thread.currentThread().getStackTrace()[2].getClassName(), className -> {
            try {
                return org.slf4j.LoggerFactory.getLogger(ClassLoader.getSystemClassLoader().loadClass(className));
            } catch (Exception e) {
                throw CodeStackException.of(e);
            }
        });
    }

}
