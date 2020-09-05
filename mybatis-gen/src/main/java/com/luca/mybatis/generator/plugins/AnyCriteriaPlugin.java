package com.luca.mybatis.generator.plugins;

import com.luca.mybatis.generator.plugins.el.*;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import static com.luca.mybatis.generator.plugins.el.MBGenerator.*;
import static com.luca.mybatis.generator.plugins.el.MBGenerator.FQJT.*;
import static org.mybatis.generator.api.dom.java.JavaVisibility.PRIVATE;
import static org.mybatis.generator.api.dom.java.JavaVisibility.PUBLIC;

/**
 * new FooExample().createCriteria().andIf("lower(lastname)","like",lastName.toLowerCase());
 */
public class AnyCriteriaPlugin extends PluginAdapter {
    private static final String AFTER_VALUE = "afterValue";
    private static final String BEFORE_VALUE = "beforeValue";
    private static final String RIGHT_VALUE = "rightValue";

    private boolean addRightIf;

    public boolean validate(List<String> warnings) {
        addRightIf = Bool.bool(Str.trim(properties.getProperty("addRightIf")), true);
        return true;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        InnerClass generatedCriteria = null;
        InnerClass criterion = null;

        for (InnerClass innerClass : topLevelClass.getInnerClasses()) {
            if ("GeneratedCriteria".equals(innerClass.getType().getShortName())) {
                generatedCriteria = innerClass;
            }
            else if ("Criterion".equals(innerClass.getType().getShortName())) {
                criterion = innerClass;
            }

        }
        if (generatedCriteria != null) {
            andEqualTo(generatedCriteria);
            andNotEqualTo(generatedCriteria);
            andIsNull(generatedCriteria);
            andIsNotNull(generatedCriteria);
            and(generatedCriteria);
            andIn(generatedCriteria, topLevelClass);
            andIf(generatedCriteria, criterion);
            if (addRightIf) andRightIf(generatedCriteria, criterion);
        }
        return true;
    }


    @Override
    public boolean sqlMapExampleWhereClauseElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (!andIf(element)) return false;
        return !addRightIf || andRightIf(element);
    }



    private void andEqualTo(InnerClass generatedCriteria) {
        and_a_op_b(generatedCriteria, "andEqualTo", "=", "andIsNull");
    }
    private void andNotEqualTo(InnerClass generatedCriteria) {
        and_a_op_b(generatedCriteria, "andNotEqualTo", "<>", "andIsNotNull");
    }
    private void andIsNull(InnerClass generatedCriteria) {
        and_a_op_null(generatedCriteria, "andIsNull", "is");
    }
    private void andIsNotNull(InnerClass generatedCriteria) {
        and_a_op_null(generatedCriteria, "andIsNotNull", "is not");
    }
    private void and(InnerClass generatedCriteria) {
        generatedCriteria.addMethod(method(
            PUBLIC, CRITERIA, "and", new Parameter(FullyQualifiedJavaType.getStringInstance(), "condition"), body(
                "addCriterion(condition);",
                "return (Criteria)this;"
        )));
    }
    private void andIf(InnerClass generatedCriteria, InnerClass criterion) {
        generatedCriteria.addMethod(method(
            PUBLIC, CRITERIA, "andIf", param(STRING, "field"), param(STRING, "operator"), param(OBJECT, "value"), body(
                "return andIf(field, operator, value, null, null);"
        )));

        String invalidConditionIdent = "`+field.replaceAll(`[^0-9A-Za-z param.()]`,``)+`";
        generatedCriteria.addMethod(method(
            PUBLIC, CRITERIA, "andIf", parameters(
                param(STRING, "field"), param(STRING, "operator"), param(OBJECT, "value"), param(STRING, BEFORE_VALUE), param(STRING, AFTER_VALUE)
            ), body(
                "if (field == null || field.trim().length() == 0) { field = `null`; }",
                format("if (operator == null) { addCriterion(`1=2 /* %s.operator */ `); return (Criteria)this; }", invalidConditionIdent),
                format("if (value == null) { addCriterion(`1=2 /* %s.value */ `); return (Criteria)this; }", invalidConditionIdent),
                "if (value instanceof java.util.Date) value = new java.sql.Date(((java.util.Date)value).getTime());",
                "String condition = field + ` `+operator+` `;",
                "Criterion c = new Criterion(condition, value);",
                format("if (%1$s != null) c.%1$s = %1$s;", BEFORE_VALUE),
                format("if (%1$s != null) c.%1$s = %1$s;", AFTER_VALUE),
                "criteria.add(c);",
                "return (Criteria)this;"
            )
        ));

        MBGenerator.addStringField(criterion, AFTER_VALUE);
        MBGenerator.addStringField(criterion, BEFORE_VALUE);
    }

    private void and_a_op_b(InnerClass generatedCriteria, String method, String operator, String nullFn) {
        generatedCriteria.addMethod(method(
            PUBLIC, CRITERIA, method, param(STRING, "field"), param(OBJECT, "value"), body(
                format("if (value == null) return %s(field);", nullFn),
                "if (value instanceof java.util.Date) value = new java.sql.Date(((java.util.Date)value).getTime());",
                format("addCriterion(field+` %s `, value, field);", operator),
                "return (Criteria)this;"
        )));
    }

    private void and_a_op_null(InnerClass generatedCriteria, String method, String operator) {
        generatedCriteria.addMethod(method(
        PUBLIC, CRITERIA, method, param(STRING, "field"), body(
            format("addCriterion(field+` %s null`);", operator),
            "return (Criteria)this;"
        )));
    }

    private void andRightIf(InnerClass generatedCriteria, InnerClass criterion) {
        generatedCriteria.addMethod(method(
            PUBLIC, CRITERIA, "andRightIf", param(OBJECT, "value"), param(STRING, "operator"), param(STRING, "field"), body(
                "return andRightIf(value, operator, field, null, null);"
        )));

        String invalidConditionIdent = "`+field.replaceAll(`[^0-9A-Za-z param.()]`,``)+`";
        generatedCriteria.addMethod(method(
            PUBLIC, CRITERIA, "andRightIf", parameters(
                param(OBJECT, "value"), param(STRING, "operator"), param(STRING, "field"), param(STRING, BEFORE_VALUE), param(STRING, AFTER_VALUE)
            ), body(
                "if (field == null || field.trim().length() == 0) { field = `null`; }",
                format("if (operator == null) { addCriterion(`1=2 /* %s.operator */ `); return (Criteria)this; }", invalidConditionIdent),
                format("if (value == null) { addCriterion(`1=2 /* %s.value */ `); return (Criteria)this; }", invalidConditionIdent),
                "if (value instanceof java.util.Date) value = new java.sql.Date(((java.util.Date)value).getTime());",
                "String condition = ` `+operator+` `+field;",
                "Criterion c = new Criterion(condition, value);",
                format("if (%1$s != null) c.%1$s = %1$s;", BEFORE_VALUE),
                format("if (%1$s != null) c.%1$s = %1$s;", AFTER_VALUE),
                format("c.%s = true;", RIGHT_VALUE),
                "criteria.add(c);",
                "return (Criteria)this;"
            )
        ));

        MBGenerator.addStringField(criterion, AFTER_VALUE);
        MBGenerator.addStringField(criterion, BEFORE_VALUE);
        MBGenerator.addBoolField(criterion, RIGHT_VALUE);
    }

    private void andIn(InnerClass generatedCriteria, TopLevelClass topLevelClass) {
        List<Method> newMethods = new ArrayList<>();
        boolean toInteger = false;
        boolean toLong = false;
        for (Method m : generatedCriteria.getMethods()) {
            String name = m.getName();
            if (!name.endsWith("In") && !name.endsWith("NotIn")) continue;
            FullyQualifiedJavaType arg = m.getParameters().get(0).getType();
            if (arg.getShortName().endsWith("<Integer>")) {
                toInteger = overloadAndXxIn(newMethods, m, int[].class, null, Integer.class) || toInteger;
                toInteger = overloadAndXxIn(newMethods, m, long[].class, null, Integer.class) || toInteger;
                toInteger = overloadAndXxIn(newMethods, m, Number[].class, null, Integer.class) || toInteger;
                //toInteger = overloadAndXxIn(newMethods, m, List.class, Long.class, Integer.class) || toInteger;
                toInteger = overloadAndXxIn(newMethods, m, Set.class, Number.class, Integer.class) || toInteger;
            }
            else if (arg.getShortName().endsWith("<Long>")) {
                toLong = overloadAndXxIn(newMethods, m, int[].class, null, Long.class) || toLong;
                toLong = overloadAndXxIn(newMethods, m, long[].class, null, Long.class) || toLong;
                toLong = overloadAndXxIn(newMethods, m, Number[].class, null, Long.class) || toLong;
                //toLong = overloadAndXxIn(newMethods, m, List.class, Integer.class, Long.class) || toLong;
                toLong = overloadAndXxIn(newMethods, m, Set.class, Number.class, Long.class) || toLong;
            }
        }
        if (!newMethods.isEmpty()) {
            if (toInteger) addToIntTypeConverters(generatedCriteria);
            if (toLong) addToLongTypeConverters(generatedCriteria);
            for (Method m : newMethods)
                generatedCriteria.addMethod(m);
            topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.Set"));
        }
    }

    private boolean overloadAndXxIn(List<Method> list, Method base, Class<?> arg, Class<?> item, Class<?> to) {
        StringBuilder sb = new StringBuilder();
        String typeName = null, componentTypeName = null;
        if (arg.isArray()) {
            sb.append("array");
            item = arg.getComponentType();
        } else if (arg == List.class) {
            sb.append("list");
            typeName = "List";
        } else if (arg == Set.class) {
            sb.append("set");
            typeName = "Set";
        }
        sb.append("Of");
        if (item == int.class) {
            sb.append("Int");
            componentTypeName = "int";
        }
        else if (item == Integer.class) {
            sb.append("Integer");
            componentTypeName = "java.lang.Integer";
        }
        else if (item == long.class) {
            sb.append("Long");
            componentTypeName = "long";
        } else if (item == Long.class) {
            sb.append("Long");
            componentTypeName = "java.lang.Long";
        } else if (item == Number.class) {
            sb.append("Number");
            componentTypeName = "java.lang.Number";
        }

        if (!arg.isArray() && componentTypeName != null) {
            if (Modifier.isAbstract(item.getModifiers()))
                componentTypeName = "? extends " + componentTypeName;
        }

        sb.append("ToListOf");
        if (to == int.class || to == Integer.class)
            sb.append("Integer");
        else if (to == long.class || to == Long.class)
            sb.append("Long");

        String converter = sb.toString();
        Method m = new Method();
        m.setName(base.getName());
        m.setReturnType(base.getReturnType());
        m.setVisibility(base.getVisibility());

        Parameter param = base.getParameters().get(0);
        FullyQualifiedJavaType type;
        if (typeName == null) {
            if (componentTypeName == null) return false;
            type = new JavaArray(componentTypeName);
        }
        else if (componentTypeName != null) {
            type = new FullyQualifiedJavaType(typeName+"<"+componentTypeName+">");
        }
        else {
            type = new FullyQualifiedJavaType(typeName);
        }
        m.addParameter(new Parameter(type, param.getName()));
        m.addBodyLine("return "+m.getName()+"("+converter+"("+param.getName()+"));");
        list.add(m);
        return true;
    }

    private void addToIntTypeConverters(InnerClass generatedCriteria) {
        String collectionOfWrappers =
            "if (values != null) " +
            "for (%s i : values) " +
                "if (i != null && i.longValue() >= Integer.MIN_VALUE && i.longValue() <= Integer.MAX_VALUE) " +
                    "list.add(i.intValue());";

        generatedCriteria.addMethod(method(
            PRIVATE, LIST_OF_INTEGER, "arrayOfIntToListOfInteger", param(new JavaArray("int"), "values"), body(
                "List<Integer> list = new ArrayList<Integer>();",
                "if (values != null) for (int i : values) list.add(i);",
                "return list;"
        )));
        generatedCriteria.addMethod(method(
            PRIVATE, LIST_OF_INTEGER, "arrayOfLongToListOfInteger", param(new JavaArray("long"), "values"), body(
                "List<Integer> list = new ArrayList<Integer>();",
                "if (values != null) for (long i : values) if (i >= Integer.MIN_VALUE && i <= Integer.MAX_VALUE) list.add((int)i);",
                "return list;"
        )));
        generatedCriteria.addMethod(method(
            PRIVATE, LIST_OF_INTEGER, "arrayOfNumberToListOfInteger", param(new JavaArray("java.lang.Number"), "values"), body(
                "List<Integer> list = new ArrayList<Integer>();",
                format(collectionOfWrappers, "Number"),
                "return list;"
        )));
        generatedCriteria.addMethod(method(
            PRIVATE, LIST_OF_INTEGER, "setOfNumberToListOfInteger", param(new FullyQualifiedJavaType("java.util.Set<? extends java.lang.Number>"), "values"), body(
                "List<Integer> list = new ArrayList<Integer>();",
                format(collectionOfWrappers, "Number"),
                "return list;"
        )));
        /*
        generatedCriteria.addMethod(method(
            PRIVATE, LIST_OF_INTEGER, "listOfLongToListOfInteger", param(new FullyQualifiedJavaType("java.util.List<java.lang.Long>"), "values"), body(
                "List<Integer> list = new ArrayList<Integer>();",
                param(collectionOfWrappers, "Long"),
                "return list;"
        )));
        */
    }

    private void addToLongTypeConverters(InnerClass generatedCriteria) {
        String collectionOfWrappers = "if (values != null) for (%s i : values) if (i != null) list.add(i.longValue());";
        generatedCriteria.addMethod(method(
            PRIVATE, LIST_OF_LONG, "arrayOfIntToListOfLong", param(new JavaArray("int"), "values"), body(
                "List<Long> list = new ArrayList<Long>();",
                "if (values != null) for (int i : values) list.add((long)i);",
                "return list;"
        )));
        generatedCriteria.addMethod(method(
            PRIVATE, LIST_OF_LONG, "arrayOfLongToListOfLong", param(new JavaArray("long"), "values"), body(
                "List<Long> list = new ArrayList<Long>();",
                "if (values != null) for (long i : values) list.add(i);",
                "return list;"
            )));
        generatedCriteria.addMethod(method(
            PRIVATE, LIST_OF_LONG, "arrayOfNumberToListOfLong", param(new JavaArray("java.lang.Number"), "values"), body(
                "List<Long> list = new ArrayList<Long>();",
                format(collectionOfWrappers, "Number"),
                "return list;"
            )));
        generatedCriteria.addMethod(method(
            PRIVATE, LIST_OF_LONG, "setOfNumberToListOfLong", param(new FullyQualifiedJavaType("java.util.Set<? extends java.lang.Number>"), "values"), body(
                "List<Long> list = new ArrayList<Long>();",
                format(collectionOfWrappers, "Number"),
                "return list;"
            )));
        /*
        generatedCriteria.addMethod(method(
            PRIVATE, LIST_OF_LONG, "listOfIntegerToListOfLong", param(new FullyQualifiedJavaType("java.util.List<? extends java.lang.Integer>"), "values"), body(
                "List<Long> list = new ArrayList<Long>();",
                param(collectionOfWrappers, "Integer"),
                "return list;"
        )));
        */
    }

    private static class JavaArray extends FullyQualifiedJavaType {
        private JavaArray(String fullTypeSpecification) {
            super(fullTypeSpecification);
        }

        @Override
        public String getFullyQualifiedName() {
            return super.getFullyQualifiedName()+"[]";
        }

        @Override
        public boolean isPrimitive() {
            return false;
        }

        @Override
        public PrimitiveTypeWrapper getPrimitiveTypeWrapper() {
            return null;
        }

        @Override
        public String getShortName() {
            return super.getShortName()+"[]";
        }
    }

    public boolean andIf(XmlElement element) {
        return traverse(element, new ReplaceText(
            new ConTextReplacer(
                "#\\{([^\\}]+?)\\.value(?:,[^\\}]+)*\\}",
                "\\${$1."+BEFORE_VALUE+"}$0\\${$1."+AFTER_VALUE+"}",
                ancestors("when").and((Predicate<Stack<XmlElement>>) attrMatches("test", "^.*\\.singleValue$"))
            ), null
        )) > 0;
    }

    public boolean andRightIf(XmlElement element) {
        return traverse(element, new FindElements() {
            @Override
            public boolean process(XmlElement parent, Element self, int position) {
                String test = Str.trim(getAttribute((XmlElement) self, "test"));
                if (test == null)
                    return false;
                String item = Str.group(test, "^(.*)\\.singleValue$");
                if (item == null)
                    return false;
                String text = Str.trim(getTextContent((XmlElement) self));
                String op = Str.group(text, "^(.+?)\\s*[\\#\\$]\\{.+$");
                if (op == null)
                    return false; // unable to get operator ("and", "or")
                String field = Str.group(text, "\\.(value(?:,[^\\}]+)?)\\}");
                if (field == null)
                    return false;
                addLater(parent, position,
                    e("when",
                        a("test", item+"."+RIGHT_VALUE),
                        String.format("%s ${%s.%s}#{%2$s.%5$s}${%2$s.%4$s} ${%2$s.condition}", op, item, BEFORE_VALUE, AFTER_VALUE, field)
                    )
                );
                return true;
            }
        }.when(ELEMENT.and(ancestorsAndSelf("when")).and(attrMatches("test", "^.*\\.singleValue$")))) > 0;
    }
}
