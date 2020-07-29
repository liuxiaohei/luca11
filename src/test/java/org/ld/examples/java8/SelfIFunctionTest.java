package org.ld.examples.java8;

import org.ld.utils.SelfCallUtil;

public class SelfIFunctionTest {

    /**
     * n的阶乘 = n * (n-1)的阶乘
     * 锻炼思维娱乐而已不建议这么写
     */
    public static void main(String[] args) {
        System.out.println(
                SelfCallUtil.run(
                        (self, n) ->
                                n <= 0
                                ? 1
                                : n * self.apply(self, n - 1),
                        10)
        ); // Expect: 3628800
    }
}
