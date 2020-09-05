package com.luca.mybatis.generator.plugins;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import com.luca.mybatis.generator.plugins.el.*;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import static org.mybatis.generator.api.dom.java.JavaVisibility.PUBLIC;
import static com.luca.mybatis.generator.plugins.el.MBGenerator.*;
import static com.luca.mybatis.generator.plugins.el.MBGenerator.FQJT.*;

/**
 * List<Integer> list = Foo.distinctBarId(fooList);
 */
@Deprecated
public class DistinctPlugin extends IntrospectorPlugin {
    private final static String SKIP    = "-";
    private final static String DEFAULT_DISTINCT_VARCHAR_COLUMNS  = "(?i)(.*[^A-Za-z]|^)(uid|name|src|code)([^A-Za-z].*|$)";
    private final static String TABLE_SPECIFIC_PREFIX             = "table:";

    private String distinctMethodPrefix;
    private String distinctMethodReturnType;
    private String distinctMethodReturnTypeImpl;
    private String firstMethodName;
    private String singleMethodName;
    private Pattern distinctVarcharColumns;
    private List<Pair<Pattern, Pattern>> tableSpecific = new ArrayList<Pair<Pattern, Pattern>>();
    private JavaVersion targetJavaVersion;

    @Override
    public boolean validate(List<String> warnings) {
        distinctVarcharColumns = Pattern.compile(Objects.nvl(Str.trim(getPropertyByRegexp("(?i)distinct-?varchar-?columns")), DEFAULT_DISTINCT_VARCHAR_COLUMNS));
        for (Enumeration<Object> keys = properties.keys(); keys.hasMoreElements(); ) {
            String key = (String)keys.nextElement();
            if (key.startsWith(TABLE_SPECIFIC_PREFIX))
                tableSpecific.add(Pair.of(Pattern.compile(key.substring(TABLE_SPECIFIC_PREFIX.length())), Pattern.compile(properties.getProperty(key))));
        }

        distinctMethodPrefix = Objects.nvl(Str.trim(getPropertyByRegexp("(?i)distinct-?method-?prefix")), "distinct");
        distinctMethodReturnType = Objects.nvl(Str.trim(getPropertyByRegexp("(?i)distinct-?method-?return-?type")), "java.util.List");
        distinctMethodReturnTypeImpl = Objects.nvl(Str.trim(getPropertyByRegexp("(?i)distinct-?method-?return-?type-?impl")), "java.util.ArrayList");
        firstMethodName = Objects.nvl(Str.trim(getPropertyByRegexp("(?i)first-?method-?name")), "first");
        singleMethodName = Objects.nvl(Str.trim(getPropertyByRegexp("(?i)single-?method-?name")), "single");

        if (!SKIP.equals(firstMethodName) && firstMethodName.equals(singleMethodName)) {
            warnings.add(Messages.load(this).get("failOnEqualMethodNames", firstMethodName));
            return false;
        }

        targetJavaVersion = Objects.nvl(JavaVersion.parse(getPropertyByRegexp("(?i)target(-?java(-?version)?)?|java(-?version)?")), JavaVersion.java5);
        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> columns;

        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            columns = introspectedTable.getNonBLOBColumns();
        } else {
            columns = introspectedTable.getAllColumns();
        }

        generateDistinct(topLevelClass, columns, introspectedTable);

        return true;
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        generateDistinct(topLevelClass, introspectedTable.getPrimaryKeyColumns(), introspectedTable);
        return true;
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        generateDistinct(topLevelClass, introspectedTable.getAllColumns(), introspectedTable);
        return true;
    }

    private String itemTypeName(FullyQualifiedJavaType fqJavaType, String column, String table, String tableConfiguration) {
        String fqTypeName = fqJavaType.getFullyQualifiedName();
        String targetType = null;
        if ("int".equals(fqTypeName) || "java.lang.Integer".equals(fqTypeName)) {
            targetType = "java.lang.Integer";
        } else if ("long".equals(fqTypeName) || "java.lang.Long".equals(fqTypeName))
            targetType = "java.lang.Long";
        else if ("short".equals(fqTypeName) || "java.lang.Short".equals(fqTypeName))
            targetType = "java.lang.Short";
        else if ("string".equals(fqTypeName.toLowerCase()) || "java.lang.String".equals(fqTypeName)) {
            if (distinctVarcharColumns.matcher(column).matches())
                targetType = "java.lang.String";
            for (Pair<Pattern, Pattern> rule : tableSpecific) {
                if (targetType == null && rule.a().matcher(table).matches())
                    if (rule.b().matcher(column).matches())
                        targetType = "java.lang.String";
            }
            if (targetType == null && tableConfiguration != null)
                for (StringTokenizer st = new StringTokenizer(tableConfiguration, ",; \t\r\n", false); st.hasMoreTokens(); ) {
                    if (column.equalsIgnoreCase(st.nextToken()))
                        targetType = "java.lang.String";
                }
        }
        return targetType;
    }

    private String distinctMethod(String javaProperty) {
        return distinctMethodPrefix + camel(javaProperty);
    }

    /**
     * Generates <tt>distinct*</tt> methods.
     * @param topLevelClass
     *            the class to which the method will be added
     * @param introspectedColumns
     *            column definitions of this class and any superclass of this
     *            class
     * @param introspectedTable
     *            the table corresponding to this class
     */
    protected void generateDistinct(TopLevelClass topLevelClass, List<IntrospectedColumn> introspectedColumns, IntrospectedTable introspectedTable) {
        String shortName = topLevelClass.getType().getShortName();
        assert shortName != null;
        FullyQualifiedJavaType argType = new FullyQualifiedJavaType("java.util.Collection<" + topLevelClass.getType().getFullyQualifiedName() + ">");

        String table = tableName(introspectedTable);

        if (!SKIP.equals(distinctMethodPrefix)) {

            for (IntrospectedColumn introspectedColumn : introspectedColumns) {
                FullyQualifiedJavaType fqJavaType = introspectedColumn.getFullyQualifiedJavaType();
                String property = introspectedColumn.getJavaProperty();
                String getterMethod = JavaBeansUtil.getGetterMethodName(property, fqJavaType);

                String targetType = itemTypeName(fqJavaType, introspectedColumn.getActualColumnName(), table, introspectedTable.getTableConfigurationProperty("distinctVarcharColumns"));
                if (targetType == null)
                    continue;

                String targetTypeShortName = targetType.substring("java.lang.".length());
                String returnType = new FullyQualifiedJavaType(distinctMethodReturnType).getShortName() + "<" + targetTypeShortName + ">";
                String returnTypeImpl = new FullyQualifiedJavaType(distinctMethodReturnTypeImpl).getShortName() + "<" + targetTypeShortName + ">";
                String methodName = distinctMethod(property);

                if (JavaVersion.java8.isSubsetOf(targetJavaVersion)) {
                    topLevelClass.addMethod(method(
                        PUBLIC, STATIC, new FullyQualifiedJavaType(returnType), methodName, param(argType, "beans"), body(
                            format("if (beans == null || beans.isEmpty()) return new %s();", returnTypeImpl),
                            format("return beans.stream().map(%s::%s).unordered().distinct().collect(java.util.stream.Collectors.toList());", shortName, getterMethod)
                    )));
                }
                else {
                    topLevelClass.addMethod(method(
                        PUBLIC, STATIC, new FullyQualifiedJavaType(returnType), methodName, param(argType, "beans"), body(
                            format("%s list = new %s();", returnType, returnTypeImpl),
                            "if (beans == null || beans.isEmpty()) return list;",
                            "if (beans.size() == 1)",
                            "{",
                            format("%s bean = beans.iterator().next();", shortName),
                            "if (bean == null) return list;",
                            format("%s v = bean.%s();", targetTypeShortName, getterMethod),
                            "if (v != null) list.add(v);",
                            "return list;",
                            "}",
                            format("Set<%s> set = new LinkedHashSet<%1$s>();", targetTypeShortName),
                            format("for (%s bean : beans) {", shortName),
                            format("%s v = bean.%s();", targetTypeShortName, getterMethod),
                            "if (v != null) set.add(v);",
                            "}",
                            "if (!set.isEmpty()) list.addAll(set);",
                            "return list;"
                    ))/*.javadoc(introspectedColumn.getActualColumnName())*/);
                }
            }
        }

        if (!SKIP.equals(singleMethodName)) {
            topLevelClass.addMethod(method(
                PUBLIC, STATIC, topLevelClass.getType(), singleMethodName, param(argType, "beans"), body(
                    "if (beans == null || beans.size() != 1) return null;",
                    "return beans.iterator().next();"
            )));
        }

        if (!SKIP.equals(firstMethodName)) {
            topLevelClass.addMethod(method(
                PUBLIC, STATIC, topLevelClass.getType(), firstMethodName, param(argType, "beans"), body(
                    "if (beans == null || beans.isEmpty()) return null;",
                    "return beans.iterator().next();"
            )));
        }

        topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.Collection"));

        if (!JavaVersion.java8.isSubsetOf(targetJavaVersion)) {
            topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.Set"));
            topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.LinkedHashSet"));
        }

        topLevelClass.addImportedType(new FullyQualifiedJavaType(distinctMethodReturnType));
        topLevelClass.addImportedType(new FullyQualifiedJavaType(distinctMethodReturnTypeImpl));
    }
}
