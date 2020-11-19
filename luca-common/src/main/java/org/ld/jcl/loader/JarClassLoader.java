package org.ld.jcl.loader;

import org.ld.jcl.resource.ClasspathResources;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reads the class bytes from jar files and other resources using ClasspathResources
 *
 */
public class JarClassLoader extends AbstractClassLoader {
    /**
     * Class cache
     */
    private final Map<String, Class> classes;
    private final ClasspathResources classpathResources;
    private char classNameReplacementChar;
    private final ProxyClassLoader localLoader = new LocalLoader();
    public JarClassLoader() {
        classpathResources = new ClasspathResources();
        classes = new ConcurrentHashMap<>();
        initialize();
    }

    /**
     * Some initialisations
     *
     */
    public void initialize() {
        addLoader(localLoader);
    }

    /**
     * Loads classes from different sources
     *
     * @param sources
     */
    public JarClassLoader(Object[] sources) {
        this();
        addAll(sources);
    }

    /**
     * Add all jar/class sources
     *
     * @param sources
     */
    public void addAll(Object[] sources) {
        for (Object source : sources) {
            add(source);
        }
    }

    /**
     * Loads local/remote source
     *
     * @param source
     */
    public void add(Object source) {
        if (source instanceof URL)
            add((URL) source);
        else if (source instanceof String)
            add((String) source);
        else
            throw new RuntimeException("Unknown Resource type");

    }

    /**
     * Loads local/remote resource
     *
     * @param resourceName
     */
    public void add(String resourceName) {
        classpathResources.loadResource(resourceName);
    }


    /**
     * Loads local/remote resource
     *
     * @param url
     */
    public void add(URL url) {
        classpathResources.loadResource(url);
    }

    /**
     * Reads the class bytes from different local and remote resources using
     * ClasspathResources
     *
     * @param className
     * @return byte[]
     */
    protected byte[] loadClassBytes(String className) {
        className = formatClassName(className);

        return classpathResources.getResource(className);
    }

    /**
     * Attempts to unload class, it only unloads the locally loaded classes by
     * JCL
     */
    @SuppressWarnings("unused")
    public void unloadClass(String className) {
        if (classes.containsKey(className)) {
            classes.remove(className);
            try {
                classpathResources.unload(formatClassName(className));
            } catch (RuntimeException e) {
                throw new RuntimeException("Something is very wrong!!!"
                        + "The locally loaded classes must be in synch with ClasspathResources", e);
            }
        } else {
            try {
                classpathResources.unload(formatClassName(className));
            } catch (RuntimeException e) {
                throw new RuntimeException("Class could not be unloaded "
                        + "[Possible reason: Class belongs to the system]", e);
            }
        }
    }

    public void close() {
        classes.clear();
        classpathResources.clear();
        removeLoader(localLoader);
    }

    /**
     * @param className
     * @return String
     */
    protected String formatClassName(String className) {
        className = className.replace('/', '~');

        if (classNameReplacementChar == '\u0000') {
            // '/' is used to map the package to the path
            className = className.replace('.', '/') + ".class";
        } else {
            // Replace '.' with custom char, such as '_'
            className = className.replace('.', classNameReplacementChar) + ".class";
        }

        className = className.replace('~', '/');
        return className;
    }

    /**
     * Local class loader
     *
     */
    class LocalLoader extends ProxyClassLoader {
        public LocalLoader() {
            order = 10;
            enabled = Configuration.isLocalLoaderEnabled();
        }

        @Override
        public Class loadClass(String className, boolean resolveIt) {
            Class result;
            byte[] classBytes;

            result = classes.get(className);
            if (result != null) {
                return result;
            }

            classBytes = loadClassBytes(className);
            if (classBytes == null) {
                return null;
            }

            result = defineClass(className, classBytes, 0, classBytes.length);

            if (result == null) {
                return null;
            }

            /*
             * Preserve package name.
             */
            if (result.getPackage() == null) {
                int lastDotIndex = className.lastIndexOf('.');
                String packageName = (lastDotIndex >= 0) ? className.substring(0, lastDotIndex) : "";
                definePackage(packageName, null, null, null, null, null, null, null);
            }

            if (resolveIt)
                resolveClass(result);

            classes.put(className, result);
            return result;
        }

        @Override
        public InputStream loadResource(String name) {
            byte[] arr = classpathResources.getResource(name);
            if (arr != null) {
                return new ByteArrayInputStream(arr);
            }

            return null;
        }

        @Override
        public URL findResource(String name) {
            return classpathResources.getResourceURL(name);
        }
    }
}
