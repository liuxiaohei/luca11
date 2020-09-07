package org.ld.utils;


/**
 */
@SuppressWarnings("unused")
public class NumberUtil {

    public static boolean isValidId(Long id) {
        return (id != null && id > 0);
    }

    public static boolean isInValidId(Long id) {
        return (id == null || id <= 0);
    }

    public static boolean isValidId(Integer id) {
        return (id != null && id > 0);
    }

    public static boolean isInValidId(Integer id) {
        return (id == null || id <= 0);
    }

}
