package org.ld.nio;

import org.junit.jupiter.api.Test;
import org.ld.utils.FileUtil;

import java.io.IOException;

public class CopyFile {

    /**
     * 使用Channel : 272
     * 不使用Channel : 572
     */
    @Test
    public void createTimeTest() throws IOException {
        var path = "/Users/liudi/Downloads/test.data";
        var size = 200 * 1024 * 1024;
        FileUtil.createFileWithChannel(path);
        var time = System.currentTimeMillis();
        FileUtil.createFileWithChannel(path);
        System.out.println("使用Channel : " + (System.currentTimeMillis() - time));
        time = System.currentTimeMillis();
        FileUtil.createFileWithOutChannel(path, size);
        System.out.println("不使用Channel : " + (System.currentTimeMillis() - time));

    }

    @Test
    public void testCopyByChannel() throws IOException {
        var path = "/Users/liudi/Downloads/test.data";
        FileUtil.createFileWithChannel(path);
        var dst = "/Users/liudi/Downloads/test_dst.data";
        var time = System.currentTimeMillis();
        FileUtil.copyFileUseNIO(path, dst);
        System.out.println(System.currentTimeMillis() - time);
    }
}
