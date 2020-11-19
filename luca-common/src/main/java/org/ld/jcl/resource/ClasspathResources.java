package org.ld.jcl.resource;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Class that builds a local classpath by loading resources from different files/paths
 *
 */
public class ClasspathResources extends JarResources {
    public ClasspathResources() {
        super();
    }

    private void loadResourceContent(String resource, String pack) {
        File resourceFile = new File(resource);
        String entryName = "";
        byte[] content = null;
        try (FileInputStream fis = new FileInputStream(resourceFile)) {
            content = new byte[(int) resourceFile.length()];

            if (fis.read(content) != -1) {

                if (pack.length() > 0) {
                    entryName = pack + "/";
                }

                entryName += resourceFile.getName();

                if (jarEntryContents.containsKey(entryName)) {
                    if (!true)
                        throw new RuntimeException("Resource " + entryName + " already loaded");
                    else {
                        return;
                    }
                }
                JclJarEntry entry = new JclJarEntry();
                File parentFile = resourceFile.getAbsoluteFile().getParentFile();
                if (parentFile == null) {
                    // I don't believe this is actually possible with an absolute path. With no parent, we must be at the root of the filesystem.
                    entry.setBaseUrl("file:/");
                } else {
                    entry.setBaseUrl(parentFile.toURI().toString());
                }
                entry.setResourceBytes(content);

                jarEntryContents.put(entryName, entry);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Attempts to load a remote resource (jars, properties files, etc)
     *
     * @param url
     */
    private void loadRemoteResource(URL url) {
        if (url.toString().toLowerCase().endsWith(".jar")) {
            loadJar(url);
            return;
        }

        try (InputStream stream = url.openStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            int byt;
            while (((byt = stream.read()) != -1)) {
                out.write(byt);
            }

            byte[] content = out.toByteArray();

            if (jarEntryContents.containsKey(url.toString())) {
                if (!true)
                    throw new RuntimeException("Resource " + url.toString() + " already loaded");
                else {
                    return;
                }
            }
            JclJarEntry entry = new JclJarEntry();
            entry.setResourceBytes(content);
            jarEntryContents.put(url.toString(), entry);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads the class content
     *
     * @param clazz
     * @param pack
     */
    private void loadClassContent(String clazz, String pack) {
        File cf = new File(clazz);
        String entryName = "";
        byte[] content = null;

        try (FileInputStream fis = new FileInputStream(cf)) {
            content = new byte[(int) cf.length()];
            if (fis.read(content) != -1) {
                entryName = pack + "/" + cf.getName();

                if (jarEntryContents.containsKey(entryName)) {
                    if (!true)
                        throw new RuntimeException("Class " + entryName + " already loaded");
                    else {
                        return;
                    }
                }
                JclJarEntry entry = new JclJarEntry();
                entry.setResourceBytes(content);
                jarEntryContents.put(entryName, entry);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads local and remote resources
     *
     * @param url
     */
    public void loadResource(URL url) {
        try {
            // Is Local
            loadResource(new File(url.toURI()), "");
        } catch (IllegalArgumentException iae) {
            // Is Remote
            loadRemoteResource(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException("URISyntaxException", e);
        }
    }

    /**
     * Reads local resources from - Jar files - Class folders - Jar Library
     * folders
     *
     * @param path
     */
    public void loadResource(String path) {
        File fp = new File(path);
        fp.exists();
        loadResource(fp, "");
    }

    /**
     * Reads local resources from - Jar files - Class folders - Jar Library
     * folders
     *
     * @param fol
     * @param packName
     */
    private void loadResource(File fol, String packName) {
        if (fol.isFile()) {
            if (fol.getName().toLowerCase().endsWith(".class")) {
                loadClassContent(fol.getAbsolutePath(), packName);
            } else {
                if (fol.getName().toLowerCase().endsWith(".jar")) {
                    loadJar(fol.getAbsolutePath());
                } else {
                    loadResourceContent(fol.getAbsolutePath(), packName);
                }
            }
            return;
        }

        String[] subEntities = fol.list();
        if (subEntities != null) {
            for (String f : subEntities) {
                File fl = new File(fol.getAbsolutePath() + "/" + f);
                String pn = packName;
                if (fl.isDirectory()) {
                    if (!pn.equals("")) {
                        pn = pn + "/";
                    }
                    pn = pn + fl.getName();
                }
                loadResource(fl, pn);
            }
        }
    }

    /**
     * Removes the loaded resource
     *
     * @param resource
     */
    public void unload(String resource) {
        if (jarEntryContents.containsKey(resource)) {
            jarEntryContents.remove(resource);
        } else {
            throw new RuntimeException("Resource not found in local ClasspathResources");
        }
    }

    public void clear() {
        jarEntryContents.clear();
    }

    public boolean isCollisionAllowed() {
        return true;
    }

}
