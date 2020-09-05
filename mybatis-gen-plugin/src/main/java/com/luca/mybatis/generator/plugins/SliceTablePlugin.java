package com.luca.mybatis.generator.plugins;

import com.luca.mybatis.generator.plugins.utils.PluginUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.TableConfiguration;

import java.util.Arrays;
import java.util.List;

/**
 * ## SliceTablePlugin
 * 当数据库单表数据量过大时，可以通过水平拆分把单表分解为多表。例如，若Account表中的预期数据量过大时（也许5、6亿），可以把一张Account表分解为多张Account表。SliceTablePlugin并不会自动创建和执行拆分后的DDL，因此必须手工创建DDL并按下述约定修改表名。
 *
 * SliceTablePlugin目前支持两种分表命名方式：
 *
 * 对指定列取模。拆分后的表名为原表名_N。如：Account_0、Account_1、Account_3 ......
 * 对指定的时间类型列，按单自然月拆分。拆分后的表名为原表名_yyyyMM。如：Account_201501、Account_201502 ...... Account_201512
 * ###Xml config： ####1.利用取模
 * 假如通过Account表的id字段做分表，计划拆分为97张表。在MyBatis GeneratorXML Configuration File的<table>元素中添加两个<property>：
 *
 * <table tableName="Account_0" domainObjectName="Account">
 *     <property name="sliceMod" value="97"/>
 *     <property name="sliceColumn" value="id"/>
 * </table>
 * sliceMod指按取模方式分表，97是取模的模数
 * sliceColumn指取模所使用的列，id是具体列名
 * ####2.利用自然月
 * 假如通过Account表的create_time 字段做拆分:
 *
 * <table tableName="Account_0" domainObjectName="Account">
 *     <property name="sliceMonth" value="1"/>
 *     <property name="sliceColumn" value="create_time"/>
 * </table>
 * sliceMonth 指按自然月分表，1指按单个自然月。Note：目前只支持按单月分表，此处的值 1 无实际意义
 * sliceColumn指时间类型的列，create_time是具体列名
 * ###Java sample ####insert
 *
 * AccountMapper mapper = ...
 * Account record = new Account();
 * record.setAge(33);
 * record.setId(101);
 * record.setCreateTime(new Date());
 * mapper.insert(record);
 * // or mapper.insertSelective(record)
 * 通过取模分表时，必须调用setId并传入合适的参数
 * 通过自然月分表时，必须调用setCreateTime并传入合适的参数
 * ####read
 *
 * AccountMapper mapper = ...
 * AccountExample example = new AccountExample();
 * example.partitionFactorId(id).createCriteria().andAgeEqualTo(33);
 * // or example.partitionFactorCreateTime(new Date()).createCriteria().andAgeEqualTo(33);
 * List<Account> as = mapper.selectByExample(example);
 * 通过取模分表时，partitionFactorId方法表示分表因子是Id字段，必须调用该方法并传入合适的参数
 * 通过自然月分表时，partitionFactorCreateTime方法表示分表因子是createTime字段，必须调用该方法并传入合适的参数
 * ####update
 *
 * AccountMapper mapper = ...
 * Account record = new Account();
 * record.setAge(33);
 * AccountExample example = new AccountExample();
 * example.partitionFactorId(id).createCriteria().andAgeEqualTo(33);
 * // or example.partitionFactorCreateTime(new Date()).createCriteria().andAgeEqualTo(33);
 * mapper.updateByExampleSelective(record, example);
 * 上例的用法和read操作一样，在example对象上必须调用partitionFactorId或partitionFactorCreateTime方法。
 * 除此之外，还可以用如下方式进行update：
 *
 * AccountMapper mapper = ...
 * Account record = new Account();
 * record.setCreateTime(new Date());
 * record.setId(101);
 * // or record.setCreateTime(new Date());
 * AccountExample example = new AccountExample();
 * example.createCriteria().andAgeEqualTo(33);
 * mapper.updateByExampleSelective(record, example);
 * 由于在record对象调用了setId或setCreateTime，就无须在example对象指定分表因子。
 *
 * ####delete
 *
 * AccountMapper mapper = ...
 * AccountExample example = new AccountExample();
 * example.partitionFactorId(id).createCriteria().andAgeEqualTo(33);
 * // or example.partitionFactorCreateTime(new Date()).createCriteria().andVersionEqualTo(0);
 * mapper.deleteByExample(example);
 * 通过取模分表时，partitionFactorId方法表示分表因子是Id字段，必须调用该方法并传入合适的参数
 * 通过自然月分表时，partitionFactorCreateTime方法表示分表因子是createTime字段，必须调用该方法并传入合适的参数
 * ####other 当无法获得分表因子的值时、或者确定所操作的表名时，可以通过:
 *
 * record.setTableNameSuffix(...)取代record.setId(...)或record.setId(...)
 * example.setTableNameSuffix(...)取代example.partitionFactorId(...)
 * WARNING：由于setTableNameSuffix的参数是String类型，在 Mybatis3 的 mapper xml 中生成${}变量，这种变量不会做sql转义，而直接嵌入到sql语句中。如果以用户输入作为setTableNameSuffix的参数，会导致潜在的SQL Injection攻击，需谨慎使用。
 */
public class SliceTablePlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        if (needPartition(introspectedTable)) {
            String tableName = introspectedTable.getFullyQualifiedTableNameAtRuntime();
            String modValue = introspectedTable.getTableConfigurationProperty(MOD_VALUE);
            String month = introspectedTable.getTableConfigurationProperty(TIME_VALUE);
            if ((modValue != null && !"".equals(modValue)) || (month != null && !"".equals(month))) {
                String baseName = tableName.substring(0, tableName.lastIndexOf(UNDERLINE));
                introspectedTable.setSqlMapAliasedFullyQualifiedRuntimeTableName(baseName + SUFFIX);
                introspectedTable.setSqlMapFullyQualifiedRuntimeTableName(baseName + SUFFIX);
            }
        }
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (needPartition(introspectedTable)) {
            String tableName = introspectedTable.getFullyQualifiedTableNameAtRuntime();
            String relColumn = introspectedTable.getTableConfigurationProperty(REL_COLUMN);
            String modValue = introspectedTable.getTableConfigurationProperty(MOD_VALUE);
            String month = introspectedTable.getTableConfigurationProperty(TIME_VALUE);
            String fieldName = convertColumnName(relColumn, introspectedTable.getTableConfiguration());

            if (modValue != null && !"".equals(modValue)) {
                FullyQualifiedJavaType ptype = STRING_TYPE;
                // 确定参数类型
                for (InnerClass innerClass : topLevelClass.getInnerClasses()) {
                    if (FullyQualifiedJavaType.getGeneratedCriteriaInstance().equals(innerClass.getType())) {
                        for (Method method : innerClass.getMethods()) {
                            String FN = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1, fieldName.length());
                            if (method.getName().equals("and" + FN + "EqualTo")) {
                                Parameter parm = method.getParameters().get(0);
                                ptype = parm.getType();
                            }
                        }
                    }
                }
                String[] expression = new String[12];
                expression[0] = "if (" + fieldName + " != null ) {";
                expression[1] = "long nan = 0;";
                expression[2] = "StringBuilder sb = new StringBuilder(\"0\");";
                expression[3] = "for (char c : String.valueOf(" + fieldName + ").toCharArray()) {";
                expression[4] = "if (Character.isDigit(c)) sb.append(c);";
                expression[5] = "else nan += c;";
                expression[6] = "}";
                expression[7] = "long lid = new BigDecimal(sb.toString()).longValue();";
                expression[8] = "if(nan > 0) lid += " + modValue + " + nan;";
                expression[9] = "this." + SUFFIX_FIELD + " = (Math.abs(lid) % " + modValue + ") + \"\";";
                expression[10] = "}";
                expression[11] = "return this;";
                Method method = makePartitionMethod(ptype, topLevelClass.getType(), fieldName, tableName, expression);
                topLevelClass.addImportedType(BIGDECIMAL_TYPE);
                topLevelClass.addMethod(method);
                System.out.println("-----------------" + topLevelClass.getType().getShortName() + " add method " + method.getName() + ".");
//                PluginUtils.addProperty(SUFFIX_FIELD, topLevelClass, this.getContext(), tableName);
            } else if (month != null && !"".equals(month)) {
                String[] expression = new String[7];
                expression[0] = "if (" + fieldName + " != null ) {";
                expression[1] = "Calendar calendar = Calendar.getInstance();";
                expression[2] = "calendar.setTimeInMillis(" + fieldName + ".getTime());";
                expression[3] = "int m = calendar.get(Calendar.MONTH) + 1;";
                expression[4] = "this." + SUFFIX_FIELD + " = calendar.get(Calendar.YEAR) + (m < 10 ? \"0\" + m : \"\" + m);";
                expression[5] = "}";
                expression[6] = "return this;";
                Method method = makePartitionMethod(DATE_TYPE, topLevelClass.getType(), fieldName, tableName, expression);
                topLevelClass.addImportedType(CALENDAR_TYPE);
                topLevelClass.addMethod(method);
                System.out.println("-----------------" + topLevelClass.getType().getShortName() + " add method " + method.getName() + ".");
//                PluginUtils.addProperty(SUFFIX_FIELD, topLevelClass, this.getContext(), tableName);
            }
            PluginUtils.addProperty(SUFFIX_FIELD, STRING_TYPE, topLevelClass, this.getContext(), tableName);
        }
        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (needPartition(introspectedTable)) {
            String tableName = introspectedTable.getFullyQualifiedTableNameAtRuntime();
            PluginUtils.addProperty(SUFFIX_FIELD, STRING_TYPE, topLevelClass, this.getContext(), tableName);
        }
        return true;
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (needPartition(introspectedTable)) {
            String tableName = introspectedTable.getFullyQualifiedTableNameAtRuntime();
            PluginUtils.addProperty(SUFFIX_FIELD, STRING_TYPE, topLevelClass, this.getContext(), tableName);
        }
        return true;
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        if (needPartition(introspectedTable)) {
            String relColumn = introspectedTable.getTableConfigurationProperty(REL_COLUMN);

            if (introspectedColumn.getActualColumnName().equals(relColumn)) {
                String modValue = introspectedTable.getTableConfigurationProperty(MOD_VALUE);
                String month = introspectedTable.getTableConfigurationProperty(TIME_VALUE);
                String field = introspectedColumn.getJavaProperty();
                if (modValue != null && !"".equals(modValue)) {
                    if (!isPrime(Long.parseLong(modValue))) {
                        System.err.printf("modValue should be a prime number!!!!!!");
                        //throw new IllegalArgumentException("modValue not a prime number!!!!!!");
                    }
                    String[] expression = new String[11];
                    expression[0] = "if (this." + field + " != null ) {";
                    expression[1] = "long nan = 0;";
                    expression[2] = "StringBuilder sb = new StringBuilder(\"0\");";
                    expression[3] = "for (char c : String.valueOf(" + field + ").toCharArray()) {";
                    expression[4] = "if (Character.isDigit(c)) sb.append(c);";
                    expression[5] = "else nan += c;";
                    expression[6] = "}";
                    expression[7] = "long lid = new BigDecimal(sb.toString()).longValue();";
                    expression[8] = "if(nan > 0) lid += " + modValue + " + nan;";
                    expression[9] = "this." + SUFFIX_FIELD + " = (Math.abs(lid) % " + modValue + ") + \"\";";
                    expression[10] = "}";
                    method.addBodyLines(Arrays.asList(expression));
                    topLevelClass.addImportedType(BIGDECIMAL_TYPE);
                    System.out.println("-----------------" + topLevelClass.getType().getShortName() + " modify method " + method.getName() + " for update field " + SUFFIX_FIELD);
                } else if (month != null && !"".equals(month)) {
                    int mc = Integer.parseInt(month);
                    if (mc < 1 || mc > 12) {
                        System.err.printf("month value should in [1-12]!!!!!!");
                        throw new IllegalArgumentException("month value should in [1-12]!!!!!!");
                    }
                    String[] expression = new String[6];
                    expression[0] = "if (this." + field + " != null ) {";
                    expression[1] = "Calendar calendar = Calendar.getInstance();";
                    expression[2] = "calendar.setTimeInMillis(" + field + ".getTime());";
                    expression[3] = "int m = calendar.get(Calendar.MONTH) + 1;";
                    expression[4] = "this." + SUFFIX_FIELD + " = calendar.get(Calendar.YEAR) + (m < 10 ? \"0\" + m : \"\" + m);";
                    expression[5] = "}";
                    method.addBodyLines(Arrays.asList(expression));
                    topLevelClass.addImportedType(CALENDAR_TYPE);
                    System.out.println("-----------------" + topLevelClass.getType().getShortName() + " modify method " + method.getName() + " for update field " + SUFFIX_FIELD);
                }
            }
        }
        return true;
    }

    @Override
    public boolean sqlMapUpdateByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return this.dynamicTableName(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return this.dynamicTableName(element, introspectedTable);
    }

    @Override
    public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        System.out.println("-----------------" + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime() + " replace parameter type to " + introspectedTable.getBaseRecordType() + " of SelectByPrimaryKey in sql xml");
        return this.replaceParamType(element, introspectedTable);
    }

    @Override
    public boolean sqlMapDeleteByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        System.out.println("-----------------" + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime() + " replace parameter type to " + introspectedTable.getBaseRecordType() + " of DeleteByPrimaryKey in sql xml");
        return this.replaceParamType(element, introspectedTable);
    }

    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        System.out.println("-----------------" + interfaze.getType().getShortName() + " replace parameter type to " + introspectedTable.getBaseRecordType() + " of SelectByPrimaryKey in client class' method " + method.getName());
        return this.replaceParamType(method, introspectedTable);
    }

    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        System.out.println("-----------------" + interfaze.getType().getShortName() + " replace parameter type to " + introspectedTable.getBaseRecordType() + " of DeleteByPrimaryKey in client class' method " + method.getName());
        return this.replaceParamType(method, introspectedTable);
    }

    private Method makePartitionMethod(FullyQualifiedJavaType paramType, FullyQualifiedJavaType returnType, String fieldName, String tableName, String[] exp) {
        String methodName = PARTITION_FACTOR + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1, fieldName.length());
        Method method = new Method();
        method.setName(methodName);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(returnType);
        method.addBodyLines(Arrays.asList(exp));
        method.addParameter(new Parameter(paramType, fieldName));
        PluginUtils.addDoc(this.getContext(), method, tableName);
        return method;
    }

    protected static boolean needPartition(IntrospectedTable introspectedTable) {
        String relColumn = introspectedTable.getTableConfigurationProperty(REL_COLUMN);
        return relColumn != null && !"".equals(relColumn);
    }

    private String convertColumnName(String column, TableConfiguration configuration) {
        String name = camelcase(column.split(UNDERLINE));
        if (configuration.getColumnOverride(column) != null) {
            name = configuration.getColumnOverride(column).getJavaProperty();
        } else if (configuration.getColumnRenamingRule() != null) {
            String search = configuration.getColumnRenamingRule().getSearchString();
            String replace = configuration.getColumnRenamingRule().getReplaceString();
            name = column.replaceAll(search, replace);
        }
        return name;
    }

    private static String camelcase(String[] words) {
        StringBuilder sb = new StringBuilder(words[0].toLowerCase());
        for (int i = 1; i < words.length; i++) {
            sb.append(words[i].substring(0, 1).toUpperCase());
            if (words[i].length() > 1) {
                sb.append(words[i].substring(1, words[i].length()).toLowerCase());
            }
        }
        return sb.toString();
    }

    private boolean replaceParamType(Method method, IntrospectedTable introspectedTable) {
        if (needPartition(introspectedTable)) {
            for (Parameter parameter : method.getParameters()) {
                try {
                    String classType = introspectedTable.getBaseRecordType();
                    java.lang.reflect.Field field = parameter.getClass().getDeclaredField("name");
                    field.setAccessible(true);
                    field.set(parameter, "record");

                    field = parameter.getClass().getDeclaredField("type");
                    field.setAccessible(true);
                    field.set(parameter, new FullyQualifiedJavaType(classType));
                } catch (NoSuchFieldException e) {
                    System.err.println("replace parameter type error" + e);
                } catch (IllegalAccessException e) {
                    System.err.println("replace parameter type error" + e);
                }
            }
        }
        return true;
    }

    private boolean replaceParamType(XmlElement element, IntrospectedTable introspectedTable) {
        if (needPartition(introspectedTable)) {
            for (Attribute attribute : element.getAttributes()) {
                if (SQL_MAP_PARAMETER_TYPE.equals(attribute.getName())) {
                    try {
                        String classType = introspectedTable.getBaseRecordType();
                        java.lang.reflect.Field field = attribute.getClass().getDeclaredField("value");
                        field.setAccessible(true);
                        field.set(attribute, classType);
                    } catch (NoSuchFieldException e) {
                        System.err.println("replace parameter type error" + e);
                    } catch (IllegalAccessException e) {
                        System.err.println("replace parameter type error" + e);
                    }
                }
            }
        }
        return true;
    }

    private boolean dynamicTableName(XmlElement element, IntrospectedTable introspectedTable) {
        if (needPartition(introspectedTable)) {
            TextElement sqlhead = (TextElement) element.getElements().get(0);
            try {
                String dynamicTableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
                String baseName = dynamicTableName.substring(0, dynamicTableName.lastIndexOf(UNDERLINE));

                String sfx = "<choose>";
                sfx += "<when test=\"record." + SUFFIX_FIELD + " != null\">" + baseName + "_${record." + SUFFIX_FIELD + "}</when>";
                sfx += "<when test=\"example." + SUFFIX_FIELD + " != null\">" + baseName + "_${example." + SUFFIX_FIELD + "}</when>";
                sfx += "<otherwise>" + baseName + "_ </otherwise>";
                sfx += "</choose>";

                java.lang.reflect.Field field = sqlhead.getClass().getDeclaredField("content");
                field.setAccessible(true);
                field.set(sqlhead, "update " + sfx);

                System.out.println("-----------------" + baseName + "generate dynamic table name base on {} in sql xml");
            } catch (NoSuchFieldException e) {
                System.err.println("generate dynamic table name error" + e);
            } catch (IllegalAccessException e) {
                System.err.println("generate dynamic table name error" + e);
            }
        }
        return true;
    }

    private static boolean isPrime(long N) {
        if (N < 2) return false;
        for (int i = 2; i * i <= N; i++) {
            if (N % i == 0) return false;
        }
        return true;
    }

    private final static String REL_COLUMN = "sliceColumn";
    private final static String MOD_VALUE = "sliceMod";
    private final static String TIME_VALUE = "sliceMonth";
    private final static String SUFFIX_FIELD = "tableNameSuffix";

    private final static FullyQualifiedJavaType STRING_TYPE = new FullyQualifiedJavaType("java.lang.String");
    private final static FullyQualifiedJavaType DATE_TYPE = new FullyQualifiedJavaType("java.util.Date");
    private final static FullyQualifiedJavaType CALENDAR_TYPE = new FullyQualifiedJavaType("java.util.Calendar");
    private final static FullyQualifiedJavaType BIGDECIMAL_TYPE = new FullyQualifiedJavaType("java.math.BigDecimal");

    private final static String UNDERLINE = "_";
    private final static String PARTITION_FACTOR = "partitionFactor";
    private final static String SUFFIX = "_${" + SUFFIX_FIELD + "}";

    private final static String SQL_MAP_PARAMETER_TYPE = "parameterType";

}
