package org.ld.examples;

import akka.actor.typed.ActorSystem;
import org.junit.jupiter.api.Test;
import org.ld.actors.HelloWorldMain;
import org.ld.utils.SnowflakeIdWorker;
import org.ld.utils.JsonUtil;
import org.ld.utils.SystemClock;
import org.ld.utils.ZLogger;

import java.util.stream.Stream;

public class DemoTest {

    /**
     * 无限流
     */
    @Test
    public void infiniteStream() {
        Stream.generate(JsonUtil::getShortUuid).limit(1000000).forEach(e -> ZLogger.newInstance().info("" + e));
    }

    /**
     * 无限流
     */
    @Test
    public void infiniteStream1() {
        Stream.iterate(0, i -> ++i).limit(1000).forEach(e -> ZLogger.newInstance().info("" + e));
    }

    public static void main(String... args) throws InterruptedException {
        System.out.println(SystemClock.now());
        System.out.println(SystemClock.now());
        System.out.println(SystemClock.now());
        Thread.sleep(1000);
        System.out.println(SystemClock.now());
        System.out.println(SystemClock.now());
    }

    @Test
    public void akksDemo() throws InterruptedException {
        final ActorSystem<HelloWorldMain.SayHello> system =
                ActorSystem.create(HelloWorldMain.create(), "hello");
        system.tell(new HelloWorldMain.SayHello("World"));
        system.tell(new HelloWorldMain.SayHello("Akka"));
        Thread.sleep(10000);
    }

    @Test
    public void idWorkerDemo() {
        for (int i = 0; i < 1000; i++) {
            long id = SnowflakeIdWorker.get();
            System.out.println(id);
        }
    }
}
