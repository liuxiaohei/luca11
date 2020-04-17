package org.ld.examples;

import org.junit.Test;
import org.ld.utils.LoggerUtil;

import java.util.concurrent.CompletableFuture;

/**
 * 单例模式，确保一个类只有一个实例，它又分为饿单例模式（类加载时实例化一个对象给自己的引用，如果对象很大是对内存一种巨大的浪费）和
 * 懒单例模式（调用取得实例的方法如getInstance时才会实例化对象）。而懒单例模式稍稍复杂下，主要是要考虑两点是否Lazy
 * 初始化和多线程安全。双重检查锁定是在输入同步块之前和之后检查惰性初始化对象的状态以确定对象是否被初始化。如果没有对float或int以外的任何可变实例进行额外的同步，
 * 它就不能以平台独立的方式可靠地工作。使用双重检查锁定对任何其他类型的基元或可变对象进行延迟初始化会使第二个线程使用未初始化或部分初始化的成员，而第一个线程仍在创建它，并使程序崩溃。
 * <p>
 * 所以以下是现在推荐的单例模式
 */
public class SingletonsDemo {

    private SingletonsDemo(String aaa) {
        System.out.println(aaa);
    }

    public SingletonsDemo() {
    }

    static class SingletonsDemoHolder {
        static SingletonsDemo singletonsDemo = new SingletonsDemo("test");
    }

    @Test
    public void demo() {
        System.out.println("start");                                         // 开始
        SingletonsDemo singletonsDemo = SingletonsDemoHolder.singletonsDemo; // 懒加载
        System.out.println("step1");                                         // 继续
        SingletonsDemo singletonsDemo1 = SingletonsDemoHolder.singletonsDemo;// 已经懒加载不会重复new
        System.out.println("end");                                           // 结束
    }
}
