package org.ld.jcl.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ld.jcl.loader.JarClassLoader;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class JclSwapEntry {
    private ClassLoader originLoader;
    private JarClassLoader newLoader;

    public void clear() {
        this.originLoader = null;
        this.newLoader = null;
    }
}
