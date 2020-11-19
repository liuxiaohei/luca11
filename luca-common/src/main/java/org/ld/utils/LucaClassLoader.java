package org.ld.utils;

import org.ld.exception.CodeStackException;
import org.ld.uc.UCSupplier;

import java.io.File;
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
        return Optional.ofNullable(super.findClass(name)).orElseThrow(() -> new ClassNotFoundException(name));
    }

    private static URL[] getURLs(String[] paths) {
        var dirs = new ArrayList<String>();
        Stream.of(paths).peek(dirs::add).forEach(path -> collectDirs(path, dirs));
        var urls = new ArrayList<URL>();
        dirs.stream().map(path -> {
            var allJars = new File(path).listFiles(pathname -> pathname.getName().endsWith(".jar"));
            var jarURLs = new ArrayList<URL>(Optional.ofNullable(allJars).map(e -> e.length).orElse(0));
            Optional.ofNullable(allJars).ifPresent(e -> Stream.of(e).forEach(allJar -> FunctionUtil.runWithUC(() -> jarURLs.add(allJar.toURI().toURL()))));
            return jarURLs;
        }).forEach(urls::addAll);
        return urls.toArray(new URL[0]);
    }

    private static void collectDirs(String path, List<String> collector) {
        var current = new File(path);
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
    public <T> T run(UCSupplier<T> function) {
        final var storeClassLoader = Thread.currentThread().getContextClassLoader();
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
