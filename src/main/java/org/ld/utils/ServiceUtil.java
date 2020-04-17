package org.ld.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import org.ld.exception.CodeStackException;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ld
 */
@SuppressWarnings("unused")
public class ServiceUtil {

    /**
     * 遍历对象的属性并转换成 指定泛型的ConfigList 对象
     * 其中String 类型不会出现 ""str""类型的春初而是直接存储
     */
    public static <T> List<T> toConfigList(Object o, Class<?> clazz, BiFunction<String, String, T> fieldTFunction) {
        try {
            final String str = new ObjectMapper().writeValueAsString(o);
            final JsonNode node =  new ObjectMapper().readTree(str);
            if (null == node) {
                return new ArrayList<>();
            }
            return Stream.of(clazz.getDeclaredFields())
                    .map(e -> {
                        final String value;
                        if (e.getType().equals(String.class)) {
                            value = Optional.ofNullable(node.get(e.getName()))
                                    .filter(s -> !(s instanceof NullNode))
                                    .map(JsonNode::asText) //
                                    .orElse(null);
                        } else {
                            try {
                                value = Optional.ofNullable(new ObjectMapper().writeValueAsString(node.get(e.getName())))
                                        .filter(s -> !"null".equals(s))
                                        .orElse(null);
                            } catch (JsonProcessingException ex) {
                                throw new CodeStackException(ex);
                            }
                        }
                        if (null == value) {
                            return null;
                        }
                        return fieldTFunction.apply(e.getName(), value);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new CodeStackException(e);
        }
    }

    /**
     * ConfigList转换回对象 toConfigList 方法的逆运算
     */
    public static <T, R> R configList2Bean(List<T> configList, Class<R> clazz, Function<T, String> nameGetter, Function<T, String> valueGetter) {
        final Map<String, Class<?>> map = Stream.of(clazz.getDeclaredFields()).collect(Collectors.toMap(Field::getName, Field::getType));
        final Map<String, Object> stringObjectMap = Optional.ofNullable(configList).orElseGet(ArrayList::new).stream().collect(Collectors.toMap(nameGetter, e -> {
            final String valueStr = valueGetter.apply(e);
            final Class<?> valueType = map.get(nameGetter.apply(e));
            if (String.class.equals(valueType)) {
                return valueStr;
            }
            try {
                return new ObjectMapper().convertValue(new ObjectMapper().readTree(valueStr), valueType);
            } catch (Exception e1) {
                throw new CodeStackException(e1);
            }
        }));
        try {
            final JsonNode node =  new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(stringObjectMap));
            return new ObjectMapper().convertValue(node, clazz);
        } catch (Exception e) {
            throw new CodeStackException(e);
        }
    }
}
