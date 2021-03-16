package org.ld;

import org.ld.exception.CodeStackException;

import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.ResultSet;

/**
 * 如何加载本地的驱动jar包连接jdbc
 */
@SuppressWarnings("unchecked") // instanceof
public class DriverTest {

    public static void main(String[] args) {
        try {
            var sysProps = System.getProperties();
            sysProps.put("user", "sa");
            sysProps.put("password", "myPassword");
            var driverClass = (Class<Driver>) URLClassLoader.newInstance(new URL[]{
                    new URL("file:/Users/xxx/Downloads/jtds-1.3.1.jar")
            }).loadClass("net.sourceforge.jtds.jdbc.Driver");//加载类
            try (var conn = driverClass.getDeclaredConstructor().newInstance().connect("jdbc:jtds:sybase://172.26.2.23:5000/tempdb", sysProps);
                 var stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ) {
                stmt.execute("INSERT INTO table_name_date  VALUES (DEFAULT, DEFAULT)");
            }
        } catch (Exception e) {
            throw CodeStackException.of(e);
        }
    }
}