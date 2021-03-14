package org.ld.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.node.NullNode;
import lombok.extern.slf4j.Slf4j;
import org.ld.exception.CodeStackException;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.io.InputStream;
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
@Slf4j
public class JsonUtil {

    public static <T> List<T> json2List(String json, Class<T> cls) {
        var objectMapper = new ObjectMapper();
        var type = objectMapper.getTypeFactory().constructCollectionType(List.class, cls);
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new CodeStackException(e);
        }
    }

    /**
     * 深拷贝对象
     */
    public static <T> T copyObj(T t, Class<T> clazz) {
        if (null == t) {
            return null;
        }
        try {
            T t1 = clazz.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(t, t1);
            return t1;
        } catch (Exception e) {
            log.error(e.getMessage());
            return json2Obj(obj2Json(t), clazz);
        }
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

    /**
     * json字符串转对象
     */
    public static <T> T json2Obj(String json, Class<T> cls) {
        final List<String> unknownProperties = new ArrayList<>();
        var handler = new DeserializationProblemHandler() {
            @Override
            @SuppressWarnings("all")
            public boolean handleUnknownProperty(DeserializationContext ctxt,
                                                 JsonParser jp,
                                                 JsonDeserializer<?> deserializer,
                                                 Object beanOrClass, String propertyName)
                    throws IOException {
                var beanClass = Optional.of(beanOrClass).filter(e -> e instanceof Class).map(e -> (Class) e).orElseGet(beanOrClass::getClass);
                unknownProperties.add(beanClass.getSimpleName() + "." + propertyName + ':' + jp.getValueAsString());
                return true;
            }
        };
        try {
            var objectMapper = new ObjectMapper().addHandler(handler);
            var t = objectMapper.readValue(json, cls);
            if (!unknownProperties.isEmpty()) {
                log.warn("unknown properties: " + obj2PrettyJson(unknownProperties));
            }
            return t;
        } catch (JsonProcessingException e) {
            var id = getShortUuid();
            log.error(id + " json转换异常:" + json);
            log.error(id + " className" + cls.getName());
            throw new CodeStackException(e);
        }
    }

    public static Map<String,Object> stream2Map(InputStream is) throws IOException {
        var reader = new org.codehaus.jackson.map.ObjectMapper().reader(Map.class);
        return reader.readValue(is);
    }

    public static Map<String, String> json2StringMap(String json) {
        if (StringUtil.isBlank(json)) return Collections.emptyMap();
        try {
            return new ObjectMapper().readValue(json, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new CodeStackException(e);
        }
    }

    public static <T> T getResponse(InputStream is,String key,Class<T> tClass) {
        try {
            return new ObjectMapper().convertValue(new ObjectMapper().readTree(StringUtil.stream2String(is)).findValue(key), tClass);
        } catch (Exception e) {
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
}
