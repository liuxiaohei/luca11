package org.ld.nio;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class CopyFile {

    @Test
    public void createTimeTest() throws IOException {
        var path = "/Users/admin/logs/test.data";
        var size = 200 * 1024 * 1024;
        createFileWithChannel(path);
        var time = System.currentTimeMillis();
        createFileWithChannel(path);
        System.out.println("使用Channel : " + (System.currentTimeMillis() - time));
        time = System.currentTimeMillis();
        createFileWithOutChannel(path, size);
        System.out.println("不使用Channel : " + (System.currentTimeMillis() - time));

    }

    @Test
    public void testCopyByChannel() throws IOException {
        var path = "/Users/admin/logs/test.data";
        createFileWithChannel(path);
        var dst = "/Users/admin/logs/test_dst.data";
        var time = System.currentTimeMillis();
        copyFileUseNIO(path, dst);
        System.out.println(System.currentTimeMillis() - time);
    }

    /**
     * 创建 文件
     */
    private static void createFileWithChannel(String path) throws IOException {
        var file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                System.out.println("创建文件夹失败");
            }
            if (!file.createNewFile()) {
                System.out.println("创建文件失败");
            }
        }
        try (var fos = new FileOutputStream(file)) {
            var channel = fos.getChannel();
            var buffer = ByteBuffer.allocate(200 * 1024 * 1024);
            int i = 1;
            while (buffer.position() < buffer.limit() - 50) {
                buffer.putInt(i++);
            }
            buffer.flip();
            channel.write(buffer);
            fos.flush();
        }
    }

    /**
     * 创建 文件
     */
    private static void createFileWithOutChannel(String path, int size) throws IOException {
        var file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                System.out.println("创建文件夹失败");
            }
            if (!file.createNewFile()) {
                System.out.println("创建文件失败");
            }
        }
        try (var fos = new FileOutputStream(file)) {
            var b = new byte[1024 * 4];
            for (var i = 0; i < 1024; i++) {
                b[i] = (byte) (i % 2);
            }
            for (var i = 1; i < size / b.length; i++) {
                fos.write(b);
            }
            fos.flush();
        }
    }

    public static void copyFileUseNIO(String src, String dst) throws IOException {
        try (var fi = new FileInputStream(new File(src));
             var fo = new FileOutputStream(new File(dst));
             var inChannel = fi.getChannel();//获得传输通道channel
             var outChannel = fo.getChannel()) {
            var buffer = ByteBuffer.allocate(1024); //创建buffer
            while (true) {
                int eof = inChannel.read(buffer);
                if (eof == -1) {
                    break;
                }
                buffer.flip(); //重设一下buffer的position=0，limit=position
                outChannel.write(buffer);
                buffer.clear();//写完要重置buffer，重设position=0,limit=capacity
            }
        }
    }
}
