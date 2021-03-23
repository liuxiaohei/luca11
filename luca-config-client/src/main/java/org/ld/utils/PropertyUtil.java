package org.ld.utils;

import org.ld.beans.ConfigChange;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.*;

/**
 * 通过对比刷新前属性和刷新后的属性 提取出变化的属性
 */
public class PropertyUtil {

    private static final List<String> standardSources = Arrays.asList(
            "systemProperties",
            "systemEnvironment",
            "jndiProperties",
            "servletConfigInitParams",
            "servletContextInitParams",
            "configurationProperties"
    );

    /**
     * 获取配置变化的结构体
     */
    public static Map<String, ConfigChange> contrast(MutablePropertySources beforeSources, MutablePropertySources afterSources) {
        final HashMap<String, ConfigChange> result = new HashMap<>();
        final Map<String, Object> before = extract(beforeSources);
        final Map<String, Object> after = extract(afterSources);
        before.keySet().forEach(key -> {
            Object b = before.get(key);
            Object a = after.get(key);
            if (!after.containsKey(key)) {
                result.put(key, null);
            } else if ((b != null || a != null) && (b == null || !b.equals(a))) {
                result.put(key, new ConfigChange(String.valueOf(before.get(key)), String.valueOf(after.get(key))));
            }
        });
        return result;
    }

    /**
     * 抽取配置信息内容
     */
    private static Map<String, Object> extract(MutablePropertySources propertySources) {
        final Map<String, Object> result = new HashMap<>();
        final List<PropertySource<?>> sources = new ArrayList<>();
        propertySources.forEach(source -> sources.add(0, source));
        sources.stream().filter(source -> !standardSources.contains(source.getName())).forEach(source -> extract(source, result));
        return result;
    }

    /**
     * PropertySource 中的遍历属性拍平 并放入result集合中
     */
    private static void extract(PropertySource<?> parent, Map<String, Object> result) {
        if (parent instanceof CompositePropertySource) {
            try {
                final List<PropertySource<?>> sources = new ArrayList<>();
                ((CompositePropertySource) parent).getPropertySources().forEach(source -> sources.add(0, source));
                sources.forEach(source -> extract(source, result));
            } catch (Exception ignored) {
            }
        } else if (parent instanceof EnumerablePropertySource) {
            Arrays.stream(((EnumerablePropertySource<?>) parent).getPropertyNames())
                    .forEach(key -> result.put(key, parent.getProperty(key)));
        }
    }
}
