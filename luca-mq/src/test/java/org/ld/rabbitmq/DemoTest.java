package org.ld.rabbitmq;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class DemoTest {

    public static void main(String... args) throws InterruptedException {
        AtomicInteger atomicInteger = new AtomicInteger(0);// 0 t1 可写 1 t1 正在写 2 t2 可写 3 t2 正在写
        var t1 = new Thread(() -> IntStream.range(1,10).forEach(i -> {
            while (!atomicInteger.compareAndSet(0,1)) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.print(i + " ");
            atomicInteger.set(2);
        }));
        var t2 = new Thread(() -> Arrays.asList("a","b","c","d","e","f","g","h","i").forEach(i -> {
            while (!atomicInteger.compareAndSet(2,3)) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.print(i + " ");
            atomicInteger.set(0);
        }));
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }
}
