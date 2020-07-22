package org.ld.examples.java8;

import org.junit.jupiter.api.Test;
import org.ld.exception.CodeStackException;

/**
 * https://www.cnblogs.com/panchanggui/p/9668284.html
 */
public class ThreadInterruptDemo {

    // 如何停掉一个处在await状态的IO任务
    // 如果是sql 要调用sql statement 的 canle 方法
    @Test
    public void demo() throws InterruptedException {
//        Statement s = null;
//        s.cancel();
        var a = new Thread(() -> {
            System.out.println("HelloWorld");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new CodeStackException(e);
            }
            System.out.println("HelloWorld1");
            System.out.println("HelloWorld2");
            System.out.println("HelloWorld3");
            System.out.println("HelloWorld4");
        });
        a.start();
        Thread.sleep(2000);
        a.interrupt();
        Thread.sleep(10000);
    }
}
