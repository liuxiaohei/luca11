package org.ld.examples.nio;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class CopyFile {

    @Test
    public void createTimeTest() throws IOException {
        String path = "/Users/admin/logs/test.data";
        int size = 200 * 1024 * 1024;
        createFileWithChannel(path, size);
        long time = System.currentTimeMillis();
        createFileWithChannel(path, size);
        System.out.println("使用Channel : " + (System.currentTimeMillis() - time));

        time = System.currentTimeMillis();
        createFileWithOutChannel(path, size);
        System.out.println("不使用Channel : " + (System.currentTimeMillis() - time));

    }

    @Test
    public void testCopyByChannel() throws IOException {
        String path = "/Users/admin/logs/test.data";
        int size = 20 * 1024 * 1024;
        createFileWithChannel(path, size);
        String dst = "/Users/admin/logs/test_dst.data";
        long time = System.currentTimeMillis();
        copyFileUseNIO(path, dst);
        System.out.println(System.currentTimeMillis() - time);
    }

    /**
     * 创建 文件
     */
    private static void createFileWithChannel(String path, int size) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
            file.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(file);
        FileChannel channel = fos.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(200 * 1024 * 1024);
        int i = 1;
        while (buffer.position() < buffer.limit() - 50) {
            buffer.putInt(i++);
        }
        buffer.flip();
        channel.write(buffer);
        fos.flush();
        fos.close();
    }

    /**
     * 创建 文件
     */
    private static void createFileWithOutChannel(String path, int size) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
            file.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(file);

        byte[] b = new byte[1024 * 4];
        for (int i = 0; i < 1024; i++) {
            b[i] = (byte) (i % 2);
        }
        for (int i = 1; i < size / b.length; i++) {
            fos.write(b);
        }
        fos.flush();
        fos.close();
    }

    public static void copyFileUseNIO(String src, String dst) throws IOException {
        FileInputStream fi = new FileInputStream(new File(src));
        FileOutputStream fo = new FileOutputStream(new File(dst));
        //获得传输通道channel
        FileChannel inChannel = fi.getChannel();
        FileChannel outChannel = fo.getChannel();
        //创建buffer
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (true) {
            int eof = inChannel.read(buffer);
            if (eof == -1) {
                break;
            }
            //重设一下buffer的position=0，limit=position
            buffer.flip();
            //开始写
            outChannel.write(buffer);
            //写完要重置buffer，重设position=0,limit=capacity
            buffer.clear();
        }
        inChannel.close();
        outChannel.close();
        fi.close();
        fo.close();
    }
}
