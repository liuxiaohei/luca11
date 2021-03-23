package org.ld.utils;

import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class IOUtil {

    /**
     * 输入流读取数据
     * 实现简单 但是 1 不能边写边读 2 需要足够大的内存
     */
    @SneakyThrows
    public static ByteArrayOutputStream readStream(InputStream stream) {
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

    /**
     * 代替jdk1.8 的ioUtils
     */
    public static byte[] readFully(InputStream var0, int var1, boolean var2) throws IOException {
        byte[] var3 = new byte[0];
        if (var1 == -1) {
            var1 = 2147483647;
        }

        int var6;
        for(int var4 = 0; var4 < var1; var4 += var6) {
            int var5;
            if (var4 >= var3.length) {
                var5 = Math.min(var1 - var4, var3.length + 1024);
                if (var3.length < var4 + var5) {
                    var3 = Arrays.copyOf(var3, var4 + var5);
                }
            } else {
                var5 = var3.length - var4;
            }

            var6 = var0.read(var3, var4, var5);
            if (var6 < 0) {
                if (var2 && var1 != 2147483647) {
                    throw new EOFException("Detect premature EOF");
                }

                if (var3.length != var4) {
                    var3 = Arrays.copyOf(var3, var4);
                }
                break;
            }
        }

        return var3;
    }

    public static byte[] readNBytes(InputStream var0, int var1) throws IOException {
        if (var1 < 0) {
            throw new IOException("length cannot be negative: " + var1);
        } else {
            return readFully(var0, var1, true);
        }
    }
}
