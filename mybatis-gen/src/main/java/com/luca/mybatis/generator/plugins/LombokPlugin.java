package com.luca.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.*;

/**
 * 整合Lombok
 */
public class LombokPlugin extends PluginAdapter {

    private final Set<Annotations> annotations;


    public LombokPlugin() {
        annotations = new LinkedHashSet<>(Annotations.values().length);
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    /**
     * 拦截 普通字段
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        addAnnotations(topLevelClass);
        addAnnotation(topLevelClass,Annotations.BUILDER);
        return true;
    }

    /**
     * 拦截 主键
     */
    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        addAnnotations(topLevelClass);
        addAnnotation(topLevelClass,Annotations.BUILDER );
        return true;
    }

    /**
     * 拦截 blob 类型字段
     */
    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass,
                                                      IntrospectedTable introspectedTable) {
        addAnnotations(topLevelClass);
        addAnnotation(topLevelClass,Annotations.EQUALS_AND_HASHCODE);
        return true;
    }

    /**
     * Prevents all getters from being generated.
     * See SimpleModelGenerator
     */
    @Override
    public boolean modelGetterMethodGenerated(
            Method method,
            TopLevelClass topLevelClass,
            IntrospectedColumn introspectedColumn,
            IntrospectedTable introspectedTable,
            ModelClassType modelClassType) {
        return false;
    }

    /**
     * Prevents all setters from being generated
     * See SimpleModelGenerator
     */
    @Override
    public boolean modelSetterMethodGenerated(
            Method method,
            TopLevelClass topLevelClass,
            IntrospectedColumn introspectedColumn,
            IntrospectedTable introspectedTable,
            ModelClassType modelClassType) {
        return false;
    }

    /**
     * Adds the lombok annotations' imports and annotations to the class
     *
     * @param topLevelClass the partially implemented model class
     */
    private void addAnnotations(TopLevelClass topLevelClass) {
        for (Annotations annotation : annotations) {
            topLevelClass.addImportedType(annotation.javaType);
            topLevelClass.addAnnotation(annotation.asAnnotation());
        }
    }

    private void addAnnotation(TopLevelClass topLevelClass, Annotations annotation) {
        topLevelClass.addImportedType(annotation.javaType);
        topLevelClass.addAnnotation(annotation.asAnnotation());
    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);

        //@Data,@Builder,@NoArgsConstructor,@AllArgsConstructor is default annotation
        annotations.add(Annotations.DATA);
//        annotations.add(Annotations.BUILDER);
        annotations.add(Annotations.NO_ARGS_CONSTRUCTOR);
        annotations.add(Annotations.ALL_ARGS_CONSTRUCTOR);

        for (String annotationName : properties.stringPropertyNames()) {
            if (annotationName.contains(".")) {
                // Not an annotation name
                continue;
            }
            String value = properties.getProperty(annotationName);
            if (!Boolean.parseBoolean(value)) {
                // The annotation is disabled, skip it
                continue;
            }
            Annotations annotation = Annotations.getValueOf(annotationName);
            if (annotation == null) {
                continue;
            }
            String optionsPrefix = annotationName + ".";
            for (String propertyName : properties.stringPropertyNames()) {
                if (!propertyName.startsWith(optionsPrefix)) {
                    continue;
                }
                String propertyValue = properties.getProperty(propertyName);
                annotation.appendOptions(propertyName, propertyValue);
                annotations.add(annotation);
                annotations.addAll(Annotations.getDependencies(annotation));
            }
        }
    }

    private enum Annotations {
        EQUALS_AND_HASHCODE("equalsAndHashCode","@EqualsAndHashCode(callSuper = true)","lombok.EqualsAndHashCode"),
        DATA("data", "@Data", "lombok.Data"),
        BUILDER("builder", "@Builder", "lombok.Builder"),
        ALL_ARGS_CONSTRUCTOR("allArgsConstructor", "@AllArgsConstructor", "lombok.AllArgsConstructor"),
        NO_ARGS_CONSTRUCTOR("noArgsConstructor", "@NoArgsConstructor", "lombok.NoArgsConstructor"),
        ACCESSORS("accessors", "@Accessors", "lombok.experimental.Accessors"),
        TO_STRING("toString", "@ToString", "lombok.ToString");


        private final String paramName;
        private final String name;
        private final FullyQualifiedJavaType javaType;
        private final List<String> options;


        Annotations(String paramName, String name, String className) {
            this.paramName = paramName;
            this.name = name;
            javaType = new FullyQualifiedJavaType(className);
            options = new ArrayList<>();
        }

        private static Annotations getValueOf(String paramName) {
            for (Annotations annotation : Annotations.values()) {
                if (String.CASE_INSENSITIVE_ORDER.compare(paramName, annotation.paramName) == 0) {
                    return annotation;
                }
            }
            return null;
        }

        private static Collection<Annotations> getDependencies(Annotations annotation) {
            if (annotation == ALL_ARGS_CONSTRUCTOR) {
                return Collections.singleton(NO_ARGS_CONSTRUCTOR);
            } else {
                return Collections.emptyList();
            }
        }

        // A trivial quoting.
        // Because Lombok annotation options type is almost String or boolean.
        private static String quote(String value) {
            if (Boolean.TRUE.toString().equals(value) || Boolean.FALSE.toString().equals(value)) {
                return value;
            }
            // case of boolean, not passed as an array.
            return value.replaceAll("[\\w]+", "\"$0\"");
        }

        private void appendOptions(String key, String value) {
            String keyPart = key.substring(key.indexOf(".") + 1);
            String valuePart = value.contains(",") ? String.format("{%s}", value) : value;
            options.add(String.format("%s=%s", keyPart, quote(valuePart)));
        }

        private String asAnnotation() {
            if (options.isEmpty()) {
                return name;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(name);
            sb.append("(");
            boolean first = true;
            for (String option : options) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(option);
            }
            sb.append(")");
            return sb.toString();
        }
    }
}