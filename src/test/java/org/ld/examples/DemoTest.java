package org.ld.examples;

import org.junit.Test;
import org.ld.utils.LoggerUtil;
import org.ld.utils.UuidUtils;

import java.util.stream.Stream;

public class DemoTest {

    /**
     * 无限流
     */
    @Test
    public void infiniteStream() {
        Stream.generate(UuidUtils::getShortUuid).limit(1000000).forEach(e -> LoggerUtil.newInstance().info("" + e));
    }

    /**
     * 无限流
     */
    @Test
    public void infiniteStream1() {
        Stream.iterate(0, i -> ++i).limit(1000).forEach(e -> LoggerUtil.newInstance().info("" + e));
    }
}
