package org.ld.utils;

import org.ld.exception.CodeStackException;

public class SleepUtil {

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CodeStackException(e);
        }
    }
}
