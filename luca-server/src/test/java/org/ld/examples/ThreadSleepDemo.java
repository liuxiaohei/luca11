package org.ld.examples;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadSleepDemo {

    private static Logger log = LoggerFactory.getLogger(ThreadSleepDemo.class);

    /**
     * 现在推荐的sleep 方式
     */
    @Test
    public void demo() {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            log.warn("e",e);
            Thread.currentThread().interrupt();
        }
    }
}
