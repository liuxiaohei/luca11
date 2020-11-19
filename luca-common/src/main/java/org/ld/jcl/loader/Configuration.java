package org.ld.jcl.loader;

/**
 * General configuration using System properties
 */
public class Configuration {

    private static final String OSGI_BOOT_DELEGATION_STRICT = "osgi.bootdelegation.strict";
    private static final String OSGI_BOOT_DELEGATION_CLASSES = "org.osgi.framework.bootdelegation";

    public static boolean isLoaderEnabled(Class cls) {
        if (System.getProperty(cls.getName()) == null) {
            return true;
        }
        return Boolean.parseBoolean(System.getProperty(cls.getName()));
    }

    public static boolean isSystemLoaderEnabled() {
        return isLoaderEnabled(AbstractClassLoader.SystemLoader.class);
    }

    public static boolean isParentLoaderEnabled() {
        return isLoaderEnabled(AbstractClassLoader.ParentLoader.class);
    }

    public static boolean isCurrentLoaderEnabled() {
        return isLoaderEnabled(AbstractClassLoader.CurrentLoader.class);
    }

    public static boolean isLocalLoaderEnabled() {
        return isLoaderEnabled(JarClassLoader.LocalLoader.class);
    }

    public static boolean isThreadContextLoaderEnabled() {
        if (System.getProperty(AbstractClassLoader.ThreadContextLoader.class.getName()) == null) {
            return false;
        }

        return isLoaderEnabled(AbstractClassLoader.ThreadContextLoader.class);
    }
}
