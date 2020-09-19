package org.ld.java11;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Files 读写文本文件
 */
public class FilesExample {

    public static void main(String[] args) throws Exception {
        var text = "Hello biezhi.";
        Files.writeString(Paths.get("hello.txt"), text);
        var readText = Files.readString(Paths.get("hello.txt"));
        System.out.println(text.equals(readText));
        Files.delete(Paths.get("hello.txt"));
    }

}