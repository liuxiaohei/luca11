package org.ld.jcl.loader;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract class loader that can load classes from different resources
 *
 */
public abstract class AbstractClassLoader extends ClassLoader {

    private final Set<ProxyClassLoader> loaders = new ConcurrentSkipListSet<>();
    @Getter
    private final ProxyClassLoader parentLoader = new ParentLoader();
    @Getter
    private final ProxyClassLoader osgiBootLoader = new OsgiBootLoader();

    public AbstractClassLoader(ClassLoader parent) {
        super(parent);
        addDefaultLoader();
    }

    public AbstractClassLoader() {
        super();
        addDefaultLoader();
    }

    protected void addDefaultLoader() {
        synchronized (loaders) {
            loaders.add(new SystemLoader());
            loaders.add(parentLoader);
            loaders.add(new CurrentLoader());
            loaders.add(new ThreadContextLoader());
        }
    }

    public void addLoader(ProxyClassLoader loader) {
        synchronized (loaders) {
            loaders.add(loader);
        }
    }

    protected void removeLoader(ProxyClassLoader loader) {
        if (loader instanceof JarClassLoader.LocalLoader) {
            synchronized (loaders) {
                loaders.remove(loader);
            }
        }
    }

    @Override
    public Class loadClass(String className) throws ClassNotFoundException {
        return (loadClass(className, true));
    }

    /**
     * Overrides the loadClass method to load classes from other resources,
     * JarClassLoader is the only subclass in this project that loads classes
     * from jar files
     *
     * @see ClassLoader#loadClass(String, boolean)
     */
    @Override
    public Class loadClass(String className, boolean resolveIt) throws ClassNotFoundException {
        if (className == null || className.trim().isEmpty()) {
            return null;
        }
        Class clazz = null;
        // Check osgi boot delegation
        if (osgiBootLoader.isEnabled()) {
            clazz = osgiBootLoader.loadClass(className, resolveIt);
        }
        if (clazz == null) {
            synchronized (loaders) {
                for (ProxyClassLoader l : loaders) {
                    if (l.isEnabled()) {
                        clazz = l.loadClass(className, resolveIt);
                        if (clazz != null) {
                            break;
                        }
                    }
                }
            }
        }
        if (clazz == null) {
            throw new ClassNotFoundException(className);
        }
        return clazz;
    }

    /**
     * Overrides the getResource method to load non-class resources from other
     * sources, JarClassLoader is the only subclass in this project that loads
     * non-class resources from jar files
     *
     * @see ClassLoader#getResource(String)
     */
    @Override
    public URL getResource(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        URL url = null;
        // Check osgi boot delegation
        if (osgiBootLoader.isEnabled()) {
            url = osgiBootLoader.findResource(name);
        }
        if (url == null) {
            synchronized (loaders) {
                for (ProxyClassLoader l : loaders) {
                    if (l.isEnabled()) {
                        url = l.findResource(name);
                        if (url != null) {
                            break;
                        }
                    }
                }
            }
        }
        return url;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        if (name == null || name.trim().isEmpty()) {
            return Collections.emptyEnumeration();
        }
        Vector<URL> urlVector = new Vector<>();
        URL url = null;
        // Check osgi boot delegation
        if (osgiBootLoader.isEnabled()) {
            url = osgiBootLoader.findResource(name);

            if (url != null) {
                urlVector.add(url);
            }
        }
        if (url == null) {
            synchronized (loaders) {
                for (ProxyClassLoader l : loaders) {
                    if (l.isEnabled()) {
                        url = l.findResource(name);
                        if (url != null) {
                            urlVector.add(url);
                        }
                    }
                }
            }
        }
        return urlVector.elements();
    }

    /**
     * Overrides the getResourceAsStream method to load non-class resources from
     * other sources, JarClassLoader is the only subclass in this project that
     * loads non-class resources from jar files
     *
     * @see ClassLoader#getResourceAsStream(String)
     */
    @Override
    public InputStream getResourceAsStream(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        InputStream is = null;
        // Check osgi boot delegation
        if (osgiBootLoader.isEnabled()) {
            is = osgiBootLoader.loadResource(name);
        }
        if (is == null) {
            synchronized (loaders) {
                for (ProxyClassLoader l : loaders) {
                    if (l.isEnabled()) {
                        is = l.loadResource(name);
                        if (is != null) {
                            break;
                        }
                    }
                }
            }
        }
        return is;
    }

    /**
     * System class loader
     */
    class SystemLoader extends ProxyClassLoader {
        private final Logger logger = LoggerFactory.getLogger(SystemLoader.class);

        SystemLoader() {
            order = 50;
            enabled = Configuration.isSystemLoaderEnabled();
        }

        @Override
        public Class loadClass(String className, boolean resolveIt) {
            Class result;
            try {
                result = findSystemClass(className);
            } catch (ClassNotFoundException e) {
                return null;
            }

            logger.debug("Returning system class " + className);
            return result;
        }

        @Override
        public InputStream loadResource(String name) {
            InputStream is = getSystemResourceAsStream(name);

            if (is != null) {
                logger.debug("Returning system resource " + name);
                return is;
            }

            return null;
        }

        @Override
        public URL findResource(String name) {
            URL url = getSystemResource(name);

            if (url != null) {
                logger.debug("Returning system resource " + name);
                return url;
            }

            return null;
        }
    }

    /**
     * Parent class loader
     */
    class ParentLoader extends ProxyClassLoader {
        private final Logger logger = LoggerFactory.getLogger(ParentLoader.class);

        ParentLoader() {
            order = 30;
            enabled = Configuration.isParentLoaderEnabled();
        }

        @Override
        public Class loadClass(String className, boolean resolveIt) {
            Class result;

            try {
                result = getParent().loadClass(className);
            } catch (ClassNotFoundException e) {
                return null;
            }

            logger.debug("Returning class " + className + " loaded with parent classloader");
            return result;
        }

        @Override
        public InputStream loadResource(String name) {
            InputStream is = getParent().getResourceAsStream(name);

            if (is != null) {
                logger.debug("Returning resource " + name + " loaded with parent classloader");
                return is;
            }
            return null;
        }


        @Override
        public URL findResource(String name) {
            URL url = getParent().getResource(name);
            if (url != null) {
                logger.debug("Returning resource " + name + " loaded with parent classloader");
                return url;
            }
            return null;
        }
    }

    /**
     * Current class loader
     */
    static class CurrentLoader extends ProxyClassLoader {
        private final Logger logger = LoggerFactory.getLogger(CurrentLoader.class);

        CurrentLoader() {
            order = 20;
            enabled = Configuration.isCurrentLoaderEnabled();
        }

        @Override
        public Class loadClass(String className, boolean resolveIt) {
            Class result;

            try {
                result = getClass().getClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                return null;
            }

            logger.debug("Returning class " + className + " loaded with current classloader");
            return result;
        }

        @Override
        public InputStream loadResource(String name) {
            InputStream is = getClass().getClassLoader().getResourceAsStream(name);

            if (is != null) {
                logger.debug("Returning resource " + name + " loaded with current classloader");
                return is;
            }

            return null;
        }


        @Override
        public URL findResource(String name) {
            URL url = getClass().getClassLoader().getResource(name);

            if (url != null) {
                logger.debug("Returning resource " + name + " loaded with current classloader");
                return url;
            }

            return null;
        }
    }

    /**
     * Current class loader
     */
    static class ThreadContextLoader extends ProxyClassLoader {
        ThreadContextLoader() {
            order = 40;
            enabled = Configuration.isThreadContextLoaderEnabled();
        }

        @Override
        public Class loadClass(String className, boolean resolveIt) {
            Class result;
            try {
                result = Thread.currentThread().getContextClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                return null;
            }
            return result;
        }

        @Override
        public InputStream loadResource(String name) {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);

            if (is != null) {
                return is;
            }

            return null;
        }

        @Override
        public URL findResource(String name) {
            return Thread.currentThread().getContextClassLoader().getResource(name);
        }

    }

    /**
     * Osgi boot loader
     */
    final class OsgiBootLoader extends ProxyClassLoader {
        private final Logger logger = LoggerFactory.getLogger(OsgiBootLoader.class);
        @Getter
        @Setter
        private boolean strictLoading;
        @Getter
        @Setter
        private String[] bootDelagation;

        private static final String JAVA_PACKAGE = "java.";

        OsgiBootLoader() {
            strictLoading = true;
            order = 0;
        }

        @Override
        public Class loadClass(String className, boolean resolveIt) {
            Class clazz = null;

            if (enabled && isPartOfOsgiBootDelegation(className)) {
                clazz = getParentLoader().loadClass(className, resolveIt);

                if (clazz == null && strictLoading) {
                    throw new RuntimeException(new ClassNotFoundException("JCL OSGi Boot Delegation: Class " + className + " not found."));
                }

                logger.debug("Class " + className + " loaded via OSGi boot delegation.");
            }

            return clazz;
        }

        @Override
        public InputStream loadResource(String name) {
            InputStream is = null;

            if (enabled && isPartOfOsgiBootDelegation(name)) {
                is = getParentLoader().loadResource(name);

                if (is == null && strictLoading) {
                    throw new RuntimeException("JCL OSGi Boot Delegation: Resource " + name + " not found.");
                }

                logger.debug("Resource " + name + " loaded via OSGi boot delegation.");
            }

            return is;
        }

        @Override
        public URL findResource(String name) {
            URL url = null;

            if (enabled && isPartOfOsgiBootDelegation(name)) {
                url = getParentLoader().findResource(name);

                if (url == null && strictLoading) {
                    throw new RuntimeException("JCL OSGi Boot Delegation: Resource " + name + " not found.");
                }

                logger.debug("Resource " + name + " loaded via OSGi boot delegation.");
            }

            return url;
        }

        private boolean isPartOfOsgiBootDelegation(String resourceName) {
            return resourceName.startsWith(JAVA_PACKAGE);
        }

    }
}
