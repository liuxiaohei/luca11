package org.ld.examples.java8;

import org.junit.jupiter.api.Test;;
import org.ld.utils.ServiceExecutor;

import java.util.stream.IntStream;

public class ForkJoinDemo {

    public static void main(String... args) {
        ServiceExecutor.getInstance().submit(() -> IntStream.rangeClosed(1,100).parallel().forEach(e -> {
            System.out.println(Thread.currentThread().getName() + " " + e);
        })).join();
        // 亲测CFucture不可以这么玩
        System.out.println("Main is over");
    }

    @Test
    public void demo() {
        System.out.println("Hello");
    }
}
