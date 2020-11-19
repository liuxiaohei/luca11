package org.ld.jcl;

import org.ld.exception.CodeStackException;
import org.ld.utils.LucaClassLoader;
import org.ld.utils.SleepUtil;

import java.lang.reflect.Proxy;
import java.sql.Driver;
import java.util.Properties;

public class JclUtils {
    @SuppressWarnings("unchecked")
    public static <T> T proxy(Object object, Class<T> tClass) {
        try {
            return (T) Proxy.newProxyInstance(JclUtils.class.getClassLoader(),
                    new Class[]{tClass},
                    (proxy, method, args) -> object.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(object, args));
        } catch (Throwable e) {
            throw new CodeStackException(e);
        }
    }

    public static void main(String... args) {
        var props = new Properties();
        props.put("user", "root");
        props.put("password", "12345678");
        props.put("remarksReporting", "true");
        var jcl = new LucaClassLoader(new String[]{"/Users/liudi/fsdownload/Drivers/mysql/mysql-5.6"},true,null);
        var a = jcl.run(() -> {
            var raw = proxy(jcl.loadClass("com.mysql.jdbc.Driver").newInstance(), Driver.class);
            var driver = new DriverShim(raw);
            var conn = driver.connect("jdbc:mysql://127.0.0.1:3306", props);
            return conn.getMetaData();
        });
        SleepUtil.sleep(10);
    }
}