package org.ld.jcl.context;

import org.ld.jcl.loader.JarClassLoader;

public class JclThreadSwapper {

    public static JarClassLoader getSwappedJarClassLoader() {
        if (isSwapped()) {
            return ThreadContext.getBound().getNewLoader();
        }
        return null;
    }

    public static void setThreadClassLoader(JarClassLoader classLoader) {
        if (classLoader == null) {
            return;
        }
        if (isSwapped()) {
            return;
        }
        ClassLoader origin = Thread.currentThread().getContextClassLoader();
        ThreadContext.bind(new JclSwapEntry(origin, classLoader));
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    public static void restoreThreadClassLoader() {
        JclSwapEntry entry = ThreadContext.unbind();
        if (entry != null) {
            Thread.currentThread().setContextClassLoader(entry.getOriginLoader());
            entry.clear();
        }
    }

    private static boolean isSwapped() {
        return ThreadContext.isBound();
    }
}