package org.ld.utils;

import lombok.Cleanup;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("all")
public class StringUtil {

    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static boolean isEmpty(Map map) {
        return map == null || map.isEmpty();
    }

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }

    public static boolean isNotEmpty(Map map) {
        return !isEmpty(map);
    }

    public static boolean isNotEmpty(Collection collection) {
        return !isEmpty(collection);
    }

    public static <T> boolean isNotEmpty(T[] array) {
        return !isEmpty(array);
    }

    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * 生成随机字母
     */
    public static String randomChar() {
        var chars = "abcdefghijklmnopqrstuvwxyz";
        return "" + chars.charAt((int) (Math.random() * 26));
    }

    /**
     * 获取指定字符串按照delimiter分割后得到的后缀
     */
    public static String stuffix(String str, String delimiter, Supplier<String> other) {
        return Optional.ofNullable(str)
                .filter(e -> e.contains(delimiter))
                .map(e -> e.substring(e.lastIndexOf(".") + 1)).orElseGet(other);
    }

    // 遍历Stream之后会自动关闭
    @SneakyThrows
    public static String stream2String(InputStream is) {
        @Cleanup var i = is;
        if (null == is) {
            return null;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder stringBuilder = new StringBuilder();
        String str;
        while ((str = br.readLine()) != null) {
            stringBuilder.append(str);
        }
        return stringBuilder.toString();
    }

    @SneakyThrows
    public static List<String> convertStreamToStr(InputStream is) {
        final String chars = "\n";
        String result;
        if (is != null) {
            var writer = new StringWriter();
            var buffer = new char[1024];
            @Cleanup var i = is;
            var reader = new BufferedReader(new InputStreamReader(i, StandardCharsets.UTF_8));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            result = writer.toString();
        } else {
            result = "";
        }
        return Stream.of(result.split(chars)).collect(Collectors.toList());
    }

    public static String toSafeUrlPath(String path) {
        return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }

    /**
     * 将带有特殊字符的路径转换成为url可显示的路径 delimiters 为可以进行特殊处理不去转换的特殊字符集合
     */
    public static String toSafeUrlPath(String path, String... exclude) {
        if (exclude.length == 0) {
            return URLEncoder.encode(path, StandardCharsets.UTF_8);
        } else if (exclude.length == 1) {
            final var delimiter = exclude[0];
            if (delimiter.equals(path)) return path;
            final var sl = new ArrayList<String>();
            for (var a : path.split(delimiter)) {
                sl.add(URLEncoder.encode(a, StandardCharsets.UTF_8));
            }
            return String.join(delimiter, sl);
        } else {
            final var delimiter = exclude[0];
            final var sl = new ArrayList<String>();
            for (var a : path.split(delimiter)) {
                sl.add(toSafeUrlPath(a, Stream.of(exclude).skip(1).toArray(String[]::new)));
            }
            return String.join(delimiter, sl);
        }
    }

}
