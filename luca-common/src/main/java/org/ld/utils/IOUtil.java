package org.ld.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IOUtil {

    /**
     * 输入流读取数据
     * 实现简单 但是 1 不能边写边读 2 需要足够大的内存
     */
    public static ByteArrayOutputStream readStream(InputStream stream) throws IOException {
        if (stream == null) {
            return null;
        }
        byte[] b = new byte[1024 * 4];
        int len;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        while ((len = stream.read(b)) != -1) {
            outStream.write(b, 0, len);
        }
        return outStream;
    }
}
