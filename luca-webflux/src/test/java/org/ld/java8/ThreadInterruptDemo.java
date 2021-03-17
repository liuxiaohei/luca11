package org.ld.java8;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.ld.exception.CodeStackException;
import org.ld.utils.SleepUtil;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread.interrupt()不会中断线程，但会影响sleep，wait等方法的逻辑
 * https://www.cnblogs.com/panchanggui/p/9668284.html
 */
public class ThreadInterruptDemo {

    // 如何停掉一个处在await状态的IO任务
    // 如果是sql 要调用sql statement 的 canle 方法
    @Test
    @SneakyThrows
    public void demo() throws InterruptedException {
//        Statement s = null;
//        s.cancel();
        var a = new Thread(() -> {
            System.out.println("HelloWorld");
            SleepUtil.sleep(10000);
            System.out.println("HelloWorld1");
            System.out.println("HelloWorld2");
            System.out.println("HelloWorld3");
            System.out.println("HelloWorld4");
        });
        a.start();
        Thread.sleep(2000);
//        Thread.currentThread().getId();
        a.interrupt(); // 如果调到这个方法 有 InterruptedException 的方法如Sleep await会抛 InterruptedException
        Thread.sleep(10000);
    }


    @Test
    public void demo1() throws InterruptedException {
        final Map<String, FutureThreadBean<Void>> executeMap = new ConcurrentHashMap<>();
        var a = CompletableFuture.runAsync(() -> {
            executeMap.computeIfAbsent("key", key -> new FutureThreadBean<>());
            System.out.println("HelloWorld");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw CodeStackException.of(e);
            }
            System.out.println("HelloWorld1");
            System.out.println("HelloWorld2");
            System.out.println("HelloWorld3");
            System.out.println("HelloWorld4");
        }).thenRun(() -> {
            executeMap.remove("key");
        });
        FutureThreadBean<Void> futureThreadBean = executeMap.computeIfAbsent("key", key -> new FutureThreadBean<>());
        futureThreadBean.setFucture(a);
        Thread.sleep(2000);
        //中断任务
        boolean interrupted = futureThreadBean.getFucture().cancel(Boolean.TRUE);
        if (interrupted) {
            executeMap.remove("key");
        }
        futureThreadBean.getThread().interrupt();
        Thread.sleep(10000);
    }
}
