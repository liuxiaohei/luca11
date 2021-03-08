package org.ld.utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * https://www.cnblogs.com/yougewe/articles/10054119.html
 * https://mp.weixin.qq.com/s?__biz=Mzg3MzU2Njk3MA==&mid=2247492648&idx=1&sn=9f02933a4c150985fd3f5adb940f746d&chksm=cedca3bdf9ab2aab8a772adbd920b86090f76203727e12270bd9486b0f5b44d19590d2bad71a&mpshare=1&scene=1&srcid=0225Qt4wJO42L1uIKI8Na6ZT&sharer_sharetime=1614246850495&sharer_shareid=119821a79b2913a576ee4b8464d2428e#rd
 */
public class LucaReentrantLock implements Lock {

    private final Sync sync = new Sync();

    /**
     * AQS，全称：AbstractQueuedSynchronizer，是JDK提供的一个同步框架，内部维护着FIFO双向队列，即CLH同步队列
     * AQS依赖它来完成同步状态的管理（voliate修饰的state，用于标志是否持有锁）。如果获取同步状态state失败时，
     * 会将当前线程及等待信息等构建成一个Node，将Node放到FIFO队列里，同时阻塞当前线程，当线程将同步状态state释放时，
     * 会把FIFO队列中的首节的唤醒，使其获取同步状态state。
     */
    private static class Sync extends AbstractQueuedSynchronizer {

        /**
         * 独占式同步状态过程
         * 1 tryAcquire 尝试去获取锁，获取成功返回true，否则返回false。该方法由继承AQS的子类自己实现。采用了模板方法设计模式。
         * 2 addWaiter
         */


        /**
         * 是否处于占用状态
         */
        @Override
        protected boolean isHeldExclusively() {
            return getState() == 1;
        }

        /**
         * 尝试去获取锁，获取成功返回true，否则返回false。该方法由继承AQS的子类自己实现
         */
        @Override
        protected boolean tryAcquire(int arg) {
            //当状态为0是获取锁
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        /**
         * 尝试去释放同步状态，释放成功返回true，否则返回false。该方法由继承AQS的子类自己实现。采用了模板方法设计模式。
         */
        @Override
        protected boolean tryRelease(int arg) {
            //释放锁，将状态设置为0
            if (getState() == 0) {
                throw new IllegalMonitorStateException();
            }
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        Condition newCondition() {
            return new ConditionObject();
        }

    }

    @Override
    public void lock() { sync.acquire(1); }

    @Override
    public void unlock() { sync.release(1); }

    @Override
    public Condition newCondition() { return sync.newCondition(); }

    @Override
    public boolean tryLock() { return sync.tryAcquire(1); }

    @Override
    public void lockInterruptibly() { }

    @Override
    public boolean tryLock(long time, TimeUnit unit) {
        return false;
    }

    public static void main(String[] args) {
        LucaReentrantLock lock = new LucaReentrantLock();
        Condition condition = lock.newCondition();
        new Thread(() -> {
            lock.lock();
            try {
                System.out.println("进入等待!");
                condition.await(); // 释放锁
                System.out.println("接收到通知！继续执行！");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lock.unlock();
        }, "conditionAwaitThread").start();

        new Thread(() -> {
            try {
                System.out.println("模拟3秒后发送通知过！");
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lock.lock();
            System.out.println("发送通知！");
            condition.signal();
            lock.unlock();
        }, "conditionSignalThread").start();
    }
}
