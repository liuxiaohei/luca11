package org.ld.jcl.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * JarResources reads jar files and loads the class content/bytes in a HashMap
 *
 */
public class JarResources {
    private final transient Logger logger = LoggerFactory.getLogger(JarResources.class);

    protected Map<String, JclJarEntry> jarEntryContents;

    /**
     * Default constructor
     */
    public JarResources() {
        jarEntryContents = new HashMap<>();
    }

    /**
     * @param name
     * @return URL
     */
    public URL getResourceURL(String name) {

        JclJarEntry entry = jarEntryContents.get(name);
        if (entry != null) {
            if (entry.getBaseUrl() == null) {
                throw new RuntimeException("non-URL accessible resource");
            }
            try {
                return new URL(entry.getBaseUrl().toString() + name);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    /**
     * @param name
     * @return byte[]
     */
    public byte[] getResource(String name) {
        JclJarEntry entry = jarEntryContents.get(name);
        if (entry != null) {
            return entry.getResourceBytes();
        } else {
            return null;
        }
    }

    /**
     * Returns an immutable Map of all jar resources
     *
     * @return Map
     */
    public Map<String, byte[]> getResources() {

        Map<String, byte[]> resourcesAsBytes = new HashMap<>(jarEntryContents.size());

        for (Map.Entry<String, JclJarEntry> entry : jarEntryContents.entrySet()) {
            resourcesAsBytes.put(entry.getKey(), entry.getValue().getResourceBytes());
        }

        return resourcesAsBytes;
    }

    /**
     * Reads the specified jar file
     *
     * @param jarFile
     */
    public void loadJar(String jarFile) {
        logger.debug("Loading jar: {}", jarFile);
        File file = new File(jarFile);
        try (FileInputStream fis = new FileInputStream(file)) {
            String baseUrl = "jar:" + file.toURI().toString() + "!/";
            loadJar(baseUrl, fis);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads the jar file from a specified URL
     *
     * @param url
     */
    public void loadJar(URL url) {
        logger.debug("Loading jar: {}", url.toString());
        try (InputStream in = url.openStream()){
            String baseUrl = "jar:" + url.toString() + "!/";
            loadJar(baseUrl, in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the jar contents from InputStream
     * @param argBaseUrl
     *
     */
    private void loadJar(String argBaseUrl, InputStream jarStream) {
        try (BufferedInputStream bis = new BufferedInputStream(jarStream); JarInputStream jis = new JarInputStream(bis)) {
            JarEntry jarEntry = null;
            while ((jarEntry = jis.getNextJarEntry()) != null) {
                logger.debug(dump(jarEntry));

                if (jarEntry.isDirectory()) {
                    continue;
                }

                if (jarEntryContents.containsKey(jarEntry.getName())) {
                    logger.debug("Class/Resource {} already loaded; ignoring entry...", jarEntry.getName());
                    continue;
                }

                logger.debug("Entry Name: {}, Entry Size: {}", jarEntry.getName(), jarEntry.getSize());

                byte[] b = new byte[2048];
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                int len = 0;
                while ((len = jis.read(b)) > 0) {
                    out.write(b, 0, len);
                }

                // add to internal resource HashMap
                JclJarEntry entry = new JclJarEntry();
                entry.setBaseUrl(argBaseUrl);
                entry.setResourceBytes(out.toByteArray());
                jarEntryContents.put(jarEntry.getName(), entry);

                logger.debug("{}: size={}, csize={}", jarEntry.getName(), out.size(), jarEntry.getCompressedSize());

                out.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            logger.debug("Done loading. but error", e);
        }
    }

    /**
     * For debugging
     *
     * @param je
     * @return String
     */
    private String dump(JarEntry je) {
        StringBuilder sb = new StringBuilder();
        if (je.isDirectory()) {
            sb.append("d ");
        } else {
            sb.append("f ");
        }

        if (je.getMethod() == JarEntry.STORED) {
            sb.append("stored   ");
        } else {
            sb.append("defalted ");
        }

        sb.append(je.getName());
        sb.append("\t");
        sb.append(je.getSize());
        if (je.getMethod() == JarEntry.DEFLATED) {
            sb.append("/").append(je.getCompressedSize());
        }
        return (sb.toString());
    }
}
