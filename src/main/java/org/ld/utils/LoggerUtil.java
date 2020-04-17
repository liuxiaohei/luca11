package org.ld.utils;

import org.ld.exception.CodeStackException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
@SuppressWarnings("unused")
public class LoggerUtil {

    private static Map<String, org.slf4j.Logger> cache = new ConcurrentHashMap<>();

    public static org.slf4j.Logger newInstance() {
        return cache.computeIfAbsent(Thread.currentThread().getStackTrace()[2].getClassName(), className -> {
            try {
                return org.slf4j.LoggerFactory.getLogger(ClassLoader.getSystemClassLoader().loadClass(className));
            } catch (Exception e) {
                throw new CodeStackException(e);
            }
        });
    }

}
