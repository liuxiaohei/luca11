package org.ld.async.waitnotify;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * https://blog.csdn.net/qq_25958497/article/details/83620427
 */
public class WaitNotifyDemo {

//    wait()方法表示，放弃当前对资源的占有权，等啊等啊，一直等到有人通知我，我才会运行后面的代码。
//    notify()方法表示，当前的线程已经放弃对资源的占有， 通知等待的线程来获得对资源的占有权，但是只有一个线程能够从wait状态中恢复，
//    然后继续运行wait()后面的语句；
//
//    notifyAll()方法表示，当前的线程已经放弃对资源的占有， 通知所有的等待线程从wait()方法后的语句开始运行。 读出什么区别没有？
//    wait，notify，notifyAll方法必须运行在synchronized代码块内

    private static final Object syncObj = new Object();

    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
        final CountDownLatch lath = new CountDownLatch(2);
        final CyclicBarrier lathb = new CyclicBarrier(2);

        // WaitThread
        Thread wd = new Thread(() -> {
            System.out.println("wait begin enter synchronized");
            synchronized (syncObj) {
                try {
                    System.out.println("wait start");
                    // 线程等待
                    syncObj.wait();
                    System.out.println("wait end");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                lathb.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
            lath.countDown();
        });

        //
        Thread nd = new Thread(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("notify begin enter synchronized");
            synchronized (syncObj) {
                System.out.println("notify start");
                // 当前线程运行完毕后 唤醒wait线程
                syncObj.notify();
                System.out.println("notify end");

            }
            try {
                lathb.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
            lath.countDown();
        });
        wd.start();
        nd.start();

        //TODO 主线程等待子线程的三种法法
        // 调用join
        // nd.join();
        // wd.join();

        //调用CountDownLatch
        lath.await();

        //调用CyclicBarrier
        // lathb.await();
        System.out.println("=============");

    }
}
