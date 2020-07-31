package org.ld.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import org.ld.exception.CodeStackException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * json工具
 */
@SuppressWarnings("unused")
public class JsonUtil {

    public static <T> List<T> json2List(String json, Class<T> cls) {
        var jsonNode = toJsonNode(json);
        if (jsonNode == null) return Collections.emptyList();
        var objectMapper = new ObjectMapper();
        var type = objectMapper.getTypeFactory().constructCollectionType(List.class, cls);
        return objectMapper.convertValue(jsonNode, type);
    }

    public static <T> T copyObj(T t, Class<T> clazz) {
        if (null == t) {
            return null;
        }
        return json2Obj(obj2Json(t),clazz);
    }

    public static String obj2Json(Object obj) {
        var mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new CodeStackException(e);
        }
    }

    public static String obj2PrettyJson(Object obj) {
        var mapper = new ObjectMapper();
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            throw new CodeStackException(e);
        }
    }

    public static <T> T json2Obj(String json, Class<T> cls) {
        var jsonNode = toJsonNode(json);
        if (jsonNode == null) return null;
        var objectMapper = new ObjectMapper();
        return objectMapper.convertValue(jsonNode, cls);
    }

    public static Map<String, String> json2Map(String json) {
        if (StringUtil.isBlank(json)) return Collections.emptyMap();
        try {
            return new ObjectMapper().readValue(json, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new CodeStackException(e);
        }
    }

    private static JsonNode toJsonNode(String expression) {
        if (StringUtil.isBlank(expression)) return null;
        try {
            return new ObjectMapper().readTree(expression);
        } catch (IOException e) {
            throw new CodeStackException(e);
        }
    }

    /**
     * 遍历对象的属性并转换成 指定泛型的ConfigList 对象
     * 其中String 类型不会出现 ""str""类型的春初而是直接存储
     */
    public static <T> List<T> toConfigList(Object o, Class<?> clazz, BiFunction<String, String, T> fieldTFunction) {
        try {
            final String str = new ObjectMapper().writeValueAsString(o);
            final JsonNode node = new ObjectMapper().readTree(str);
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
            final JsonNode node = new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(stringObjectMap));
            return new ObjectMapper().convertValue(node, clazz);
        } catch (Exception e) {
            throw new CodeStackException(e);
        }
    }

    private static final String[] chars = new String[]{
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l",
            "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x",
            "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
            "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    /**
     * 返回8位uuid
     */
    public static String getShortUuid() {
        final var uuid = UUID.randomUUID().toString().replace("-", "");
        return IntStream.rangeClosed(0, 7).boxed()
                .map(i -> uuid.substring(i * 4, i * 4 + 4))
                .map(str -> Integer.parseInt(str, 16))
                .map(i -> i % 0x3E)
                .map(i -> chars[i])
                .collect(Collectors.joining());
    }

    /**
     * 生成随机字母
     */
    private String randomChar() {
        var chars = "abcdefghijklmnopqrstuvwxyz";
        return "" + chars.charAt((int) (Math.random() * 26));
    }

    private String stuffix(String fileName) {
        return Optional.ofNullable(fileName)
                .filter(e -> e.contains("."))
                .map(e -> e.substring(e.lastIndexOf(".") + 1)).orElse("jpg");
    }
}
