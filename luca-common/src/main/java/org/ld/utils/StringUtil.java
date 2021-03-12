package org.ld.utils;

import org.ld.exception.CodeStackException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

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
    public static String stream2String(InputStream is) {
        try (var i = is) {
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
        } catch (Exception e) {
            throw new CodeStackException(e);
        }
    }

}
