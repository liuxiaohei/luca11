package org.ld.examples;

import org.junit.Test;
import org.ld.utils.LoggerUtil;
import org.ld.utils.UuidUtils;

import java.util.stream.Stream;

public class DemoTest {

    @Test
    public void sendMail() {

    }

    /**
     * 无限流
     */
    @Test
    public void infiniteStream() {
        //Stream.iterate(0, i -> ++i).limit(1000).forEach(e -> Logger.newInstance().info(() -> "" + e));
        Stream.generate(UuidUtils::getShortUuid).limit(1000000).forEach(e -> LoggerUtil.newInstance().info("" + e));
    }

//    @Test
//    public void demo1() {
//        try {
//            final AbstractJPPFClassLoader cl = (AbstractJPPFClassLoader) getClass().getClassLoader();
//            final URL[] urls = cl.getURLs();
//            boolean found = false;
//            // is shutdown.jar already in the classpath ?
//            for (URL url : urls) {
//                if (url.toString().indexOf("shutdown.jar") >= 0) {
//                    found = true;
//                    break;
//                }
//            }
//            // if not let's add it dynamically
//            if (!found) {
//                File file = new File(jbossHome + "/bin/shutdown.jar");
//                cl.addURL(file.toURI().toURL());
//                file = new File(jbossHome + "/client/jbossall-client.jar");
//                cl.addURL(file.toURI().toURL());
//                final JarFile jar = new JarFile(file);
//                final Manifest manifest = jar.getManifest();
//                final String classPath = manifest.getMainAttributes().getValue("Class-Path");
//                final String[] libs = classPath.split("\\s");
//                final File dir = file.getParentFile();
//                for (String s : libs) cl.addURL(new File(dir, s).toURI().toURL());
//                jar.close();
//            }
//            // with shutdown.jar in the classpath, we can now invoke the ocmmand
//            // org.jboss.Shutdown.org.ld.main("-S") to shtudown the server
//            System.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
//            System.setProperty("jboss.boot.loader.name", "shutdown.bat");
//            final Class<?> clazz = cl.loadClass("org.jboss.Shutdown");
//            final Method method = clazz.getDeclaredMethod("org.ld.main", String[].class);
//            System.out.println("shutting down by invoking " + method);
//            method.invoke((Object) null, (Object) new String[]{"-S"});
//        } catch (final Exception e) {
//            e.printStackTrace();
//
//        }
//
//    }
}
