package org.ld.gray;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RibbonFilterContext {

    private static final ThreadLocal<Map<String, String>> contextHolder = new InheritableThreadLocal<>() {
        @Override
        protected Map<String, String> initialValue() {
            return new HashMap<>();
        }
    };

    public static void add(String key, String value) {
        contextHolder.get().put(key, value);
    }

    public static Map<String,String> getAttributes() {
      return Collections.unmodifiableMap(contextHolder.get());
    }

    public static void clearCurrentContext() {
        contextHolder.remove();
    }
}