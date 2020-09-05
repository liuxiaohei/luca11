package com.luca.mybatis.generator.plugins.deprecated;

import java.util.List;

import com.luca.mybatis.generator.plugins.el.Objects;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import static com.luca.mybatis.generator.plugins.el.MBGenerator.*;

/**
 * 有lombok 的时候不建议使用
 */
@Deprecated
public class ModelSettersChainPlugin extends PluginAdapter {
    private boolean generateNewSetters;
    private MethodNameGenerator gen;

    private interface MethodNameGenerator {
        public String name(String field);
    }

    private class NoPrefix implements MethodNameGenerator {
        @Override
        public String name(String field) {
            return field;
        }
    }

    private class Prefix implements MethodNameGenerator {
        private String prefix;
        public Prefix(String prefix) {
            this.prefix = prefix;
        }
        @Override
        public String name(String field) {
            return prefix + camel(field);
        }
    }

    @Override
    public boolean validate(List<String> warnings) {
        String prefix = Objects.nvl(properties.getProperty("prefix"), "set");
        if (prefix == null || prefix.trim().length() == 0)
            gen = new NoPrefix();
        else
            gen = new Prefix(prefix);
        generateNewSetters = !"set".equals(prefix);
        return true;
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        if (generateNewSetters) {
            Method alt = method(method.getVisibility(), topLevelClass.getType(), gen.name(introspectedColumn.getJavaProperty()), parameters(method));
            alt.getAnnotations().addAll(method.getAnnotations());
            if (method.getBodyLines() != null)
                for (String line : method.getBodyLines())
                    alt.addBodyLine(line);
            alt.addBodyLine("return this;");
            topLevelClass.addMethod(alt);
        } else {
            method.setReturnType(topLevelClass.getType());
            method.addBodyLine("return this;");
        }
        return true;
    }


}
