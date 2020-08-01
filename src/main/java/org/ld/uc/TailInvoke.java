package org.ld.uc;

import java.util.function.Supplier;

/**
 * 使用尾递归的类,目的是对外统一方法
 */
public class TailInvoke {

    /**
     * 统一结构的方法,获得当前递归的下一个递归
     * 用lambda懒加载的特性完成了递归中栈帧的复用,实现了函数式语言编译器的'尾递归'优化
     * @param nextFrame 下一个递归
     * @param <T>       T
     * @return 下一个递归
     */
    public static <T> TailRecursion<T> call(final TailRecursion<T> nextFrame) {
        return nextFrame;
    }

    /**
     * 结束当前递归，重写对应的默认方法的值,完成状态改为true,设置最终返回结果,设置非法递归调用
     *
     * @param value 最终递归值
     * @param <T>   T
     * @return 一个isFinished状态true的尾递归, 外部通过调用接口的invoke方法及早求值, 启动递归求值。
     */
    public static <T> TailRecursion<T> call(Supplier<T> value) {
        return new TailRecursion<>() {

            @Override
            public TailRecursion<T> apply() {
                throw new Error("递归已经结束,非法调用apply方法");
            }

            @Override
            public boolean isFinished() {
                return true;
            }

            @Override
            public T getResult() {
                return value.get();
            }
        };
    }
}
