package org.ld;

import javax.tools.ToolProvider;
import java.net.URL;
import java.net.URLClassLoader;

public class Dynamic {
    public static void main(String[] args) throws Exception {
        System.out.println(ToolProvider.getSystemJavaCompiler().run(null, null, null,
                "/Users/liudi/IdeaProjects/luca11/luca-webflux/src/test/java/first.java") == 0 ? "编译成功" : "编译失败");
        new URLClassLoader(new URL[]{new URL("file:/Users/liudi/IdeaProjects/luca11/luca-webflux/src/test/java/")})
                .loadClass("Solution")
                .getDeclaredMethod("demo", String[].class)
                .invoke(null, (Object) new String[]{});
    }
}
