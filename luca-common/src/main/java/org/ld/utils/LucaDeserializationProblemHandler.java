package org.ld.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 提取出未知属性 可打印
 */
public class LucaDeserializationProblemHandler extends DeserializationProblemHandler {

    private final Map<UnknownProperty, String> unknownProperties = new HashMap<>();

    @Override
    @SuppressWarnings("all")
    public boolean handleUnknownProperty(DeserializationContext ctxt,
                                         JsonParser jp,
                                         JsonDeserializer<?> deserializer,
                                         Object beanOrClass, String propertyName)
            throws IOException {

        Class beanClass = Optional.of(beanOrClass)
                .filter(e -> e instanceof Class)
                .map(e -> (Class) e)
                .orElseGet(beanOrClass::getClass);
        unknownProperties.put(new UnknownProperty(beanClass, propertyName), jp.getValueAsString());
        return true;
    }

    public boolean hasUnknownProperty() {
        return !unknownProperties.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("UnknownPropertyWrapper {\n");
        for (Map.Entry<UnknownProperty, String> entry : unknownProperties.entrySet()) {
            builder.append('\t')
                    .append(entry.getKey().getType().getSimpleName())
                    .append('.')
                    .append(entry.getKey().getFieldName())
                    .append(' ')
                    .append(entry.getValue())
                    .append('\n');
        }
        return builder.append('}').toString();
    }


    @AllArgsConstructor
    @Getter
    @EqualsAndHashCode
    private static class UnknownProperty {
        private final Class<?> type;
        private final String fieldName;
    }
}