package org.ld.gray.support;

import org.ld.gray.api.RibbonFilterContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RibbonFilterContextHolder {

    private static final ThreadLocal<RibbonFilterContext> contextHolder = new InheritableThreadLocal<>() {
        @Override
        protected RibbonFilterContext initialValue() {
            return new RibbonFilterContext() {
                private final Map<String, String> attributes = new HashMap<>();
                @Override
                public RibbonFilterContext add(String key, String value) {
                    attributes.put(key, value);
                    return this;
                }
                @Override
                public String get(String key) {
                    return attributes.get(key);
                }
                @Override
                public RibbonFilterContext remove(String key) {
                    attributes.remove(key);
                    return this;
                }
                @Override
                public Map<String, String> getAttributes() {
                    return Collections.unmodifiableMap(attributes);
                }
            };
        }
    };

    public static RibbonFilterContext getCurrentContext() {
        return contextHolder.get();
    }

    public static void clearCurrentContext() {
        contextHolder.remove();
    }
}