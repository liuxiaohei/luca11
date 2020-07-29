package org.ld.utils;

import org.ld.uc.SelfFunction;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public class SelfCallUtil {

    @SuppressWarnings("all")
    public static <T> T run(SelfFunction<T> selfFunction, T num) {
        return (
                (Function<SelfFunction<T>, UnaryOperator<T>>)
                        self -> n -> self.apply(self, n) // 这一行的括号里面 递归函数的定义自己调用自己
        )
                .apply(
                        selfFunction::apply
                )                                        // 这一行括号里面写的是递归函数的参数 自己如何调用自己 返回一个普通的函数
                .apply(num);                             // 调用普通函数传入参数得到结果

    }
}
