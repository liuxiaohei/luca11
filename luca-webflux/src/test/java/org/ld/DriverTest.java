package org.ld;

import lombok.Cleanup;
import lombok.SneakyThrows;

import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.ResultSet;

/**
 * 如何加载本地的驱动jar包连接jdbc
 */
@SuppressWarnings("unchecked") // instanceof
public class DriverTest {

    @SneakyThrows
    public static void main(String[] args) {
        var sysProps = System.getProperties();
        sysProps.put("user", "sa");
        sysProps.put("password", "myPassword");
        var driverClass = (Class<Driver>) URLClassLoader.newInstance(new URL[]{
                new URL("file:/Users/xxx/Downloads/jtds-1.3.1.jar")
        }).loadClass("net.sourceforge.jtds.jdbc.Driver");//加载类
        @Cleanup var conn = driverClass.getDeclaredConstructor().newInstance().connect("jdbc:jtds:sybase://172.26.2.23:5000/tempdb", sysProps);
        @Cleanup var stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        stmt.execute("INSERT INTO table_name_date  VALUES (DEFAULT, DEFAULT)");
    }
}