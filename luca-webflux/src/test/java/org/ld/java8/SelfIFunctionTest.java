package org.ld.java8;

import org.junit.jupiter.api.Test;
import org.ld.uc.TailInvoke;
import org.ld.uc.TailRecursion;
import org.ld.utils.SelfCallUtil;

public class SelfIFunctionTest {

    /**
     * n的阶乘 = n * (n-1)的阶乘
     */
    public static void main(String[] args) {
        System.out.println(
                SelfCallUtil.run(
                        (self, n) -> n <= 0 ? 1 : n * self.apply(n - 1),
                        10)
        ); // Expect: 3628800
    }

    /**
     * 阶乘计算 -- 使用尾递归接口完成
     *
     * @param result 当前递归栈的结果值 可以这样理解 对于跳出条件而言 他就是当前number 下对应的结果，对于外部调用而言它是初始的结果，
     * @param number    下一个递归需要计算的值
     * @return 尾递归接口, 调用invoke启动及早求值获得结果
     */
    public static TailRecursion<Integer> factorialTailRecursion(final int result, final int number) {
        if (number == 1) {
            return TailInvoke.call(() -> result);
        } else {
            return TailInvoke.call(() -> factorialTailRecursion(result * number, number - 1));
        }
    }

    @Test
    public void demo() {
        System.out.println(factorialTailRecursion(1, 10).invoke());
        // Expect: 3628800
    }

}
