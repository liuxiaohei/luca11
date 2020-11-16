package org.ld.utils;

import javax.validation.constraints.NotNull;
import java.util.concurrent.Callable;

/**
 * Created by Leon Wang on 11/5/19
 * TODO 非线程安全, 暂时不要用
 */
public class ClassLoaderSwapper {
    private ClassLoader storeClassLoader = null;

    private ClassLoaderSwapper() {
    }

    public static ClassLoaderSwapper newCurrentThreadClassLoaderSwapper() {
        return new ClassLoaderSwapper();
    }

    /**
     * 保存当前classLoader，并将当前线程的classLoader设置为所给classLoader
     */
    public ClassLoader setCurrentThreadClassLoader(ClassLoader classLoader) {
        if (this.storeClassLoader == null) {
            this.storeClassLoader = Thread.currentThread().getContextClassLoader();
        }
        Thread.currentThread().setContextClassLoader(classLoader);
        return this.storeClassLoader;
    }

    /**
     * 将当前线程的类加载器设置为保存的类加载
     */
    public ClassLoader restoreCurrentThreadClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (this.storeClassLoader != null) {
            Thread.currentThread().setContextClassLoader(this.storeClassLoader);
        }
        return classLoader;
    }
}
