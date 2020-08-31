package org.ld.utils;

import org.ld.uc.YCombinator;

import java.util.function.Function;

@SuppressWarnings("unused")
public class SelfCallUtil {

    /**
     * 针对输入和输出类型不同的递归
     */
    @SuppressWarnings("all")
    public static <T, R> R run(YCombinator<T, R> yCombinator, Class<R> rClass, T num) {
        return (
                (Function<YCombinator<T, R>, Function<T, R>>)
                        self -> n -> self.apply(self, n) // 这一行的括号里面 递归函数的定义自己调用自己
        )
                .apply(
                        yCombinator::apply
                )                                        // 这一行括号里面写的是递归函数的参数 自己如何调用自己 返回一个普通的函数
                .apply(num);                             // 调用普通函数传入参数得到结果

    }

    @SuppressWarnings("all")
    public static <T> T run(YCombinator<T, T> yCombinator, T t) {
        return (
                (Function<YCombinator<T, T>, Function<T, T>>)
                        self -> n -> self.apply(self, n) // 这一行的括号里面 递归函数的定义自己调用自己
        )
                .apply(
                        yCombinator::apply
                )                                        // 这一行括号里面写的是递归函数的参数 自己如何调用自己 返回一个普通的函数
                .apply(t);                             // 调用普通函数传入参数得到结果

    }
}
