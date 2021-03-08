package org.ld;

import java.util.concurrent.CyclicBarrier;

/**
 * CyclicBarrier 又叫循环珊栏
 * 会在barrier.await() 方法处阻塞 会在阻塞数目到达的时候 触发回调函数并释放所有阻塞
 *
 */
public class CyclicBarrierTest {
    public static void main(String[] args) {
        new Object();
        int threadNum = 5;
        CyclicBarrier barrier = new CyclicBarrier(
                threadNum,
                () -> System.out.println(Thread.currentThread().getName() + " 完成最后任务"));
        for (int i = 0; i < threadNum; i++) {
            new Thread(() -> {
                try {
//                    Thread.sleep(1);
                    System.out.println(Thread.currentThread().getName() + " 到达栅栏 A");
                    barrier.await();
                    System.out.println(Thread.currentThread().getName() + " 冲破栅栏 A");
//                    Thread.sleep(1);
                    System.out.println(Thread.currentThread().getName() + " 到达栅栏 B");
                    barrier.await();
                    System.out.println(Thread.currentThread().getName() + " 冲破栅栏 B");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
