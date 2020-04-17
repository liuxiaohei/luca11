package org.ld.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ld.exception.CodeStackException;

import java.io.IOException;
import java.util.*;

/**
 * json工具
 */
public class JsonUtil {

    public static <T> List<T> json2List(String json, Class<T> cls) {
        JsonNode jsonNode = toJsonNode(json);
        if (jsonNode == null) return Collections.emptyList();
        ObjectMapper objectMapper = new ObjectMapper();
        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, cls);
        return objectMapper.convertValue(jsonNode, type);
    }

    public static String obj2Json(Object obj) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new CodeStackException(e);
        }
    }

    public static <T> T json2Obj(String json, Class<T> cls) {
        JsonNode jsonNode = toJsonNode(json);
        if (jsonNode == null) return null;
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(jsonNode, cls);
    }

    public static Map<String, String> json2Map(String json) {
        if (StringUtil.isBlank(json)) return Collections.emptyMap();
        try {
            return new ObjectMapper().readValue(json, new TypeReference<Map<String, String>>() {
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
}
