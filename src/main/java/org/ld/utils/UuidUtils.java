package org.ld.utils;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("unused")
public class UuidUtils {

    private static final String[] chars = new String[]{
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l",
            "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x",
            "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
            "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    /**
     * 返回8位uuid
     */
    public static String getShortUuid() {
        final var uuid = UUID.randomUUID().toString().replace("-", "");
        return IntStream.rangeClosed(0,7).boxed()
                .map(i -> uuid.substring(i * 4, i * 4 + 4))
                .map(str -> Integer.parseInt(str,16))
                .map(i -> i % 0x3E)
                .map(i -> chars[i])
                .collect(Collectors.joining());
    }
}
