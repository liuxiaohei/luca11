/*
 * Copyright (c) 2017.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.luca.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 数据Model属性对应Column获取插件
 */
public class ModelColumnPlugin extends PluginAdapter {
    public static final String ENUM_NAME = "Column";  // 内部Enum名
    private static final Logger logger = LoggerFactory.getLogger(ModelColumnPlugin.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(List<String> warnings) {
        // 插件使用前提是targetRuntime为MyBatis3
        if (StringUtility.stringHasValue(getContext().getTargetRuntime()) && !"MyBatis3".equalsIgnoreCase(getContext().getTargetRuntime())) {
            logger.warn("chrm:插件" + this.getClass().getTypeName() + "要求运行targetRuntime必须为MyBatis3！");
            return false;
        }
        return true;
    }

    /**
     * Model Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 生成内部枚举
        InnerEnum innerEnum = new InnerEnum(new FullyQualifiedJavaType(ENUM_NAME));
        innerEnum.setVisibility(JavaVisibility.PUBLIC);
        innerEnum.setStatic(true);
        logger.debug("chrm(数据Model属性对应Column获取插件):" + topLevelClass.getType().getShortName() + "增加内部Builder类。");

        // 生成属性和构造函数
        Field columnField = new Field("column", FullyQualifiedJavaType.getStringInstance());
        columnField.setVisibility(JavaVisibility.PRIVATE);
        columnField.setFinal(true);
        innerEnum.addField(columnField);

        Method mValue = new Method("value");
        mValue.setVisibility(JavaVisibility.PUBLIC);
        mValue.setReturnType(FullyQualifiedJavaType.getStringInstance());
        mValue.addBodyLine("return this.column;");
        innerEnum.addMethod(mValue);

        Method mGetValue = new Method("getValue");
        mGetValue.setVisibility(JavaVisibility.PUBLIC);
        mGetValue.setReturnType(FullyQualifiedJavaType.getStringInstance());
        mGetValue.addBodyLine("return this.column;");
        innerEnum.addMethod(mGetValue);

        Method constructor = new Method(ENUM_NAME);
        constructor.setConstructor(true);
        constructor.addBodyLine("this.column = column;");
        constructor.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "column"));
        innerEnum.addMethod(constructor);
        logger.debug("chrm(数据Model属性对应Column获取插件):" + topLevelClass.getType().getShortName() + ".Column增加构造方法和column属性。");

        // Enum枚举
        for (IntrospectedColumn introspectedColumn : introspectedTable.getAllColumns()) {
            Field field = JavaBeansUtil.getJavaBeansField(introspectedColumn, context, introspectedTable);

            String sb = field.getName() +
                    "(\"" +
                    introspectedColumn.getActualColumnName() +
                    "\")";
            innerEnum.addEnumConstant(sb);
            logger.debug("chrm(数据Model属性对应Column获取插件):" + topLevelClass.getType().getShortName() + ".Column增加" + field.getName() + "枚举。");
        }

        // asc 和 desc 方法
        Method desc = new Method("desc");
        desc.setVisibility(JavaVisibility.PUBLIC);
        desc.setReturnType(FullyQualifiedJavaType.getStringInstance());
        desc.addBodyLine("return this.column + \" DESC\";");
        innerEnum.addMethod(desc);

        Method asc = new Method("asc");
        asc.setVisibility(JavaVisibility.PUBLIC);
        asc.setReturnType(FullyQualifiedJavaType.getStringInstance());
        asc.addBodyLine("return this.column + \" ASC\";");
        innerEnum.addMethod(asc);
        logger.debug("chrm(数据Model属性对应Column获取插件):" + topLevelClass.getType().getShortName() + ".Column增加asc()和desc()方法。");
        topLevelClass.addInnerEnum(innerEnum);
        return true;
    }
}
