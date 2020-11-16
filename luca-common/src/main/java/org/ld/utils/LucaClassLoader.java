package org.ld.utils;

import org.ld.exception.CodeStackException;
import org.ld.uc.UCSupplier;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 把传入的路径、及其子路径、以及路径中的jar文件加入到class path
 */
public class LucaClassLoader extends URLClassLoader {
    // 控制是否取消默认的 parent loading 类加载机制, 默认不取消
    private boolean interruptParentLoading = false;
    // 遇到以下类关键字, 用当前 class loader 加载而不采用默认的 parent loading policy
    private List<String> interruptKeyWords;

    public LucaClassLoader(String[] paths, boolean interruptParentLoading, List<String> interruptKeyWords) {
        this(paths, LucaClassLoader.class.getClassLoader());
        this.interruptParentLoading = interruptParentLoading;
        this.interruptKeyWords = interruptKeyWords;
    }

    public LucaClassLoader(String[] paths, ClassLoader parent) {
        super(getURLs(paths), parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (interruptParentLoading && Optional.ofNullable(interruptKeyWords).orElseGet(ArrayList::new).stream().anyMatch(v -> v.contains(name))) {
            return findClass(name);
        }
        return super.loadClass(name, resolve);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> claz;
        try {
            claz = super.findClass(name);
        } catch (ClassNotFoundException e) {
            claz = null;
        }
        if (claz != null) {
            return claz;
        }
        throw new ClassNotFoundException(name);
    }

    /**
     * 根据本地文件路径递归遍历出所有的url
     */
    private static URL[] getURLs(String[] paths) {
        List<String> dirs = new ArrayList<>();
        Stream.of(paths)
                .peek(dirs::add)
                .forEach(path -> collectDirs(path, dirs));
        List<URL> urls = new ArrayList<>();
        dirs.stream()
                .map(path -> {
                    File[] allJars = new File(path).listFiles(pathname -> pathname.getName().endsWith(".jar"));
                    List<URL> jarURLs = new ArrayList<>(Optional.ofNullable(allJars).map(e -> e.length).orElse(0));
                    for (int i = 0; i < Optional.ofNullable(allJars).map(e -> e.length).orElse(0); i++) {
                        try {
                            jarURLs.add(allJars[i].toURI().toURL());
                        } catch (MalformedURLException ignored) {
                        }
                    }
                    return jarURLs;
                })
                .forEach(urls::addAll);
        return urls.toArray(new URL[0]);
    }

    /**
     * 递归遍历指定文件夹下的所有路径
     */
    private static void collectDirs(String path, List<String> collector) {
        File current = new File(path);
        if (!current.exists() || !current.isDirectory()) {
            return;
        }
        Arrays.stream(Optional.ofNullable(current.listFiles()).orElseGet(() -> new File[0]))
                .filter(File::isDirectory)
                .map(File::getAbsolutePath)
                .peek(collector::add)
                .forEach(e -> collectDirs(e, collector));
    }

    /**
     * 使用当前的classLoader执行function 执行之后线程切换回之前的上下文
     */
    public synchronized <T> T run(UCSupplier<T> function) {
        final ClassLoader storeClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this);
            return function.get();
        } catch (Throwable e) {
            throw new CodeStackException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(storeClassLoader);
        }
    }
}
