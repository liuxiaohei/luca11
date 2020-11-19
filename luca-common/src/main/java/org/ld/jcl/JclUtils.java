package org.ld.jcl;

import org.ld.exception.CodeStackException;
import org.ld.jcl.context.JclThreadSwapper;
import org.ld.jcl.loader.JarClassLoader;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Driver;
import java.util.Properties;

public class JclUtils {
    public static <T> T proxy(Object object, Class<T> tClass) {
        return (T) Proxy.newProxyInstance(JclUtils.class.getClassLoader(),
                new Class[] {tClass},
                (proxy, method, args) -> object.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(object, args));
    }

    public static void main(String ... args) {
        Properties props = new Properties();
        props.put("user", "username");
        props.put("password", "password");
        props.put("remarksReporting", "true");
        JarClassLoader jcl = JclThreadSwapper.getSwappedJarClassLoader();
        if (jcl == null) {
            jcl = new JarClassLoader(new String[] {"driverDir"});;
            JclThreadSwapper.setThreadClassLoader(jcl);
        }
        try {
            Driver raw = JclUtils.proxy(jcl.loadClass("driverClass").newInstance(), Driver.class);
            Driver driver = new DriverShim(raw);
            Connection conn = driver.connect("jdbcUrl", props);
        } catch (Throwable e) {
            throw new CodeStackException(e);
        } finally {
            JclThreadSwapper.restoreThreadClassLoader();
        }
    }
}