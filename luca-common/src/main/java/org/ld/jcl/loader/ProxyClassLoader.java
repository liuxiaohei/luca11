package org.ld.jcl.loader;

import java.io.InputStream;
import java.net.URL;

public abstract class ProxyClassLoader implements Comparable<ProxyClassLoader> {
    // Default order
    protected int order = 5;
    // Enabled by default
    protected boolean enabled = true;

    public int getOrder() {
        return order;
    }

    /**
     * Set loading order
     *
     * @param order
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Loads the class
     *
     * @param className
     * @param resolveIt
     * @return class
     */
    public abstract Class loadClass(String className, boolean resolveIt);

    /**
     * Loads the resource
     *
     * @param name
     * @return InputStream
     */
    public abstract InputStream loadResource(String name);

    /**
     * Finds the resource
     *
     * @param name
     * @return InputStream
     */
    public abstract URL findResource(String name);

    public boolean isEnabled() {
        return enabled;
    }

    public int compareTo(ProxyClassLoader o) {
        return order - o.getOrder();
    }
}
