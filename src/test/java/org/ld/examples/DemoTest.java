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
        //Stream.iterate(0, i -> ++i).limit(1000).forEach(e -> Logger.newInstance().info(() -> "" + e));
        Stream.generate(UuidUtils::getShortUuid).limit(1000000).forEach(e -> LoggerUtil.newInstance().info("" + e));
    }
}
