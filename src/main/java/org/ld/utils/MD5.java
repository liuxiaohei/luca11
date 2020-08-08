package org.ld.utils;

/**
 * *********************************************
 * MD5 算法的Java Bean
 *
 * @author:ZHL Last Modified:10,Mar,2008
 * ***********************************************
 */

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class MD5 {

    public static char[] num_chars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F'};

    private MD5() {
    }

    public static String md5H16(String input) {
        return md5(input).substring(8, 24);
    }

    public static String md5(String input) {
        return toLowMD5String(input);
    }

    public static String toLowMD5String(String input) {
        return toMD5String(input).toLowerCase();
    }

    public static String toMD5String(String input) {
        if (input == null) {
            input = "";
        }
        return toMD5String(input.getBytes());
    }

    public static String toMD5String(byte[] input) {
        final char[] output = new char[32];
        try {
            var md = MessageDigest.getInstance("MD5");
            var by = md.digest(input);
            for (var i = 0; i < by.length; i++) {
                output[2 * i] = num_chars[(by[i] & 0xf0) >> 4];
                output[2 * i + 1] = num_chars[by[i] & 0xf];
            }
        } catch (Exception ignored) {
        }
        return new String(output);
    }


    public static String getFileMD5(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return null;
        }
        var file = new File(filePath);
        if (!file.isFile()) {
            return null;
        }
        FileInputStream in = null;
        var buffer = new byte[1024];
        int len;
        try {
            var md = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                md.update(buffer, 0, len);
            }

            var by = md.digest();
            final var output = new char[32];
            for (var i = 0; i < by.length; i++) {
                output[2 * i] = num_chars[(by[i] & 0xf0) >> 4];
                output[2 * i + 1] = num_chars[by[i] & 0xf];
            }
            return new String(output);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
