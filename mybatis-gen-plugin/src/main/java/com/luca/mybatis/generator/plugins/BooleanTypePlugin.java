package com.luca.mybatis.generator.plugins;

import com.luca.mybatis.generator.plugins.el.Str;
import com.luca.mybatis.generator.plugins.el.StringMacro;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.DefaultJavaFormatter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.PropertyRegistry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.luca.mybatis.generator.plugins.el.MBGenerator.FQJT.*;

/**
 * Provides a converter (type handler) from Java Boolean type to VARCHAR and NUMBER(x,0) columns.
 */
public class BooleanTypePlugin extends PluginAdapter {
    private String defaultTrueString;
    private String defaultFalseString;
    private int defaultTrueInt;
    private int defaultFalseInt;

    private final List<GeneratedJavaFile> javaFiles = new ArrayList<>();
    private final Set<String> handlers = new HashSet<>();

    @Override
    public boolean validate(List<String> warnings) {
        defaultTrueString = com.luca.mybatis.generator.plugins.el.Objects.nvl(Str.trim(properties.getProperty("varchar:true")), "Y");
        defaultFalseString = com.luca.mybatis.generator.plugins.el.Objects.nvl(Str.trim(properties.getProperty("varchar:false")), "N");

        defaultTrueInt = Integer.parseInt(com.luca.mybatis.generator.plugins.el.Objects.nvl(Str.trim(properties.getProperty("integer:true")), "1"));
        defaultFalseInt = Integer.parseInt(com.luca.mybatis.generator.plugins.el.Objects.nvl(Str.trim(properties.getProperty("integer:false")), "0"));

        return !com.luca.mybatis.generator.plugins.el.Objects.equals(defaultTrueString, defaultFalseString) && defaultTrueInt != defaultFalseInt;
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles() {
        return javaFiles;
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        List<BooleanColumn<?>> columns = enumerateColumns(introspectedTable);
        for (final BooleanColumn<?> column : columns) {
            column.getIntrospectedColumn().setFullyQualifiedJavaType(BOOLEAN);
            if (!handlers.contains(column.getHandlerId())) {
                Context ctx = introspectedTable.getContext();

                StringMacro sm = new StringMacro();
                sm.text("${TRUE}", column.getTRUE().toString()).text("${FALSE}", column.getFALSE().toString());
                String targetProject = ctx.getJavaClientGeneratorConfiguration().getTargetProject();
                String fileEncoding = context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING);
                JavaFormatter javaFormatter = new DefaultJavaFormatter();
                if (!handlers.contains(column.getClass().getName())) {
                    handlers.add(column.getClass().getName());
                    CompilationUnit unit = new Template(column.getAbstractHandlerClassName()+".java", column.getPackage(), column.getAbstractHandlerClassName(), sm);
                    javaFiles.add(new GeneratedJavaFile(unit, targetProject, fileEncoding, javaFormatter));
                }
                handlers.add(column.getHandlerId());
                CompilationUnit unit = new Template(column.getHandlerTemplate(), column.getPackage(), column.getHandlerClassName(), sm);
                javaFiles.add(new GeneratedJavaFile(unit, targetProject, fileEncoding, javaFormatter));
            }
            column.getIntrospectedColumn().setTypeHandler(column.getPackage()+"."+column.getHandlerClassName());
        }
    }


    private final static Pattern COLUMN = Pattern.compile("^\\s*+(.+?)\\s*+(?:\\(\\s*+(.+?)\\s*+/\\s*+(.+?)\\s*+\\))?\\s*$");

    private List<BooleanColumn<?>> enumerateColumns(IntrospectedTable introspectedTable) {
        List<BooleanColumn<?>> list = new ArrayList<>();
        String columns = com.luca.mybatis.generator.plugins.el.Objects.nvl(Str.trim(introspectedTable.getTableConfigurationProperty("boolean-integer-columns")), "");
        for (StringTokenizer st = new StringTokenizer(columns, ",;", false); st.hasMoreTokens(); ) {
            String columnDefinition = st.nextToken();
            IntegerBooleanColumn c = new IntegerBooleanColumn(introspectedTable, columnDefinition, String.valueOf(defaultTrueInt), String.valueOf(defaultFalseInt));
            if (c.isValid())
                list.add(c);
        }
        columns = com.luca.mybatis.generator.plugins.el.Objects.nvl(Str.trim(introspectedTable.getTableConfigurationProperty("boolean-varchar-columns")), "");
        for (StringTokenizer st = new StringTokenizer(columns, ",;", false); st.hasMoreTokens(); ) {
            String columnDefinition = st.nextToken();
            VarcharBooleanColumn c = new VarcharBooleanColumn(introspectedTable, columnDefinition, defaultTrueString, defaultFalseString);
            if (c.isValid())
                list.add(c);
        }
        return list;
    }

    private abstract static class BooleanColumn<T> {
        private T TRUE;
        private T FALSE;
        private String pakkage;
        private String handlerClassName;
        private String handlerId;
        private String column;
        private IntrospectedColumn introspectedColumn;

        boolean valid;

        BooleanColumn(IntrospectedTable introspectedTable, String columnDefinition, String defaultTrueString, String defaultFalseString) {
            Matcher matcher = COLUMN.matcher(columnDefinition);
            if (!matcher.matches()) {
                System.err.println("Invalid column definition: '"+columnDefinition+"'");
                return;
            }
            column = matcher.group(1);
            TRUE = cast(com.luca.mybatis.generator.plugins.el.Objects.nvl(matcher.group(2), defaultTrueString));
            FALSE = cast(com.luca.mybatis.generator.plugins.el.Objects.nvl(matcher.group(3), defaultFalseString));

            handlerId = TRUE + "/" + FALSE;
            pakkage = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType()).getPackageName();
            handlerClassName = generateClassName();

            for (IntrospectedColumn c : introspectedTable.getAllColumns()) {
                if (column.equalsIgnoreCase(c.getActualColumnName())) {
                    introspectedColumn = c;
                    break;
                }
            }
            if (introspectedColumn == null) {
                System.err.println("Column '"+column+"' not found in table "+introspectedTable.getFullyQualifiedTableNameAtRuntime());
                return;
            }

            valid = true;
        }

        public T getTRUE() {
            return TRUE;
        }

        public T getFALSE() {
            return FALSE;
        }

        public String getPackage() {
            return pakkage;
        }

        public String getHandlerClassName() {
            return handlerClassName;
        }

        public String getHandlerId() {
            return handlerId;
        }

        public String getColumn() {
            return column;
        }

        public boolean isValid() {
            return valid;
        }

        public IntrospectedColumn getIntrospectedColumn() {
            return introspectedColumn;
        }

        public abstract T cast(String s);

        public abstract String generateClassName();

        public abstract String getHandlerTemplate();

        public abstract String getAbstractHandlerClassName();
    }

    private static class VarcharBooleanColumn extends BooleanColumn<String> {
        VarcharBooleanColumn(IntrospectedTable introspectedTable, String columnDefinition, String defaultTrueString, String defaultFalseString) {
            super(introspectedTable, columnDefinition, defaultTrueString, defaultFalseString);
        }

        @Override
        public String cast(String s) {
            return s;
        }

        @Override
        public String generateClassName() {
            return "VarcharBoolean_"+toCodes(getTRUE())+"param"+toCodes(getFALSE());
        }

        @Override
        public String getHandlerTemplate() {
            return "CustomVarcharBooleanTypeHandler.java";
        }

        @Override
        public String getAbstractHandlerClassName() {
            return "AbstractVarcharBooleanTypeHandler";
        }
    }

    private static class IntegerBooleanColumn extends BooleanColumn<Integer> {
        IntegerBooleanColumn(IntrospectedTable introspectedTable, String columnDefinition, String defaultTrueString, String defaultFalseString) {
            super(introspectedTable, columnDefinition, defaultTrueString, defaultFalseString);
        }

        @Override
        public Integer cast(String s) {
            return Integer.valueOf(s);
        }

        private String toString(int v) {
            return v < 0 ? "m"+(-v) : String.valueOf(v);
        }

        @Override
        public String generateClassName() {
            return "IntegerBoolean_"+toString(getTRUE()) +"param"+toString(getFALSE());
        }

        @Override
        public String getHandlerTemplate() {
            return "CustomIntegerBooleanTypeHandler.java";
        }

        @Override
        public String getAbstractHandlerClassName() {
            return "AbstractIntegerBooleanTypeHandler";
        }
    }

    private static String toCodes(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            String h = Integer.toHexString(s.charAt(i));
            if (h.length() < 4) sb.append("0000".substring(h.length()));
            sb.append(h);
        }
        return sb.toString();
    }

    private static class Template implements CompilationUnit {
        private String content;
        private final FullyQualifiedJavaType type;

        public Template(String resource, String pkg, String className, StringMacro macro) {
            String uri = "/"+getClass().getPackage().getName().replace('.', '/')+"/"+resource;
            InputStream is = getClass().getResourceAsStream(uri);
            if (is != null) {
                try {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    byte[] buf = new byte[32768];
                    for (int count; (count = is.read(buf)) >= 0; ) os.write(buf, 0, count);
                    is.close();
                    macro.text("${PACKAGE}", pkg).text("${CLASS_NAME}", className);
                    this.content = macro.replaceAll(new String(os.toByteArray(), StandardCharsets.UTF_8));
                } catch (IOException e) {
                    System.err.println("Failed to load "+uri);
                    e.printStackTrace();
                }
            } else {
                System.err.println("Unable to load "+uri+" - resource not found");
            }
            type = new FullyQualifiedJavaType(pkg+"."+className);
        }

        @Override
        public String getFormattedContent() {
            return content;
        }

        @Override
        public Set<FullyQualifiedJavaType> getImportedTypes() {
            return null;
        }

        @Override
        public Set<String> getStaticImports() {
            return null;
        }

        @Override
        public FullyQualifiedJavaType getSuperClass() {
            return null;
        }

        @Override
        public boolean isJavaInterface() {
            return false;
        }

        @Override
        public boolean isJavaEnumeration() {
            return false;
        }

        @Override
        public Set<FullyQualifiedJavaType> getSuperInterfaceTypes() {
            return null;
        }

        @Override
        public FullyQualifiedJavaType getType() {
            return type;
        }

        @Override
        public void addImportedType(FullyQualifiedJavaType fullyQualifiedJavaType) {

        }

        @Override
        public void addImportedTypes(Set<FullyQualifiedJavaType> fullyQualifiedJavaTypes) {

        }

        @Override
        public void addStaticImport(String s) {

        }

        @Override
        public void addStaticImports(Set<String> strings) {

        }

        @Override
        public void addFileCommentLine(String s) {

        }

        @Override
        public List<String> getFileCommentLines() {
            return null;
        }
    }
}
