package org.ld.leetcode;

import org.junit.jupiter.api.Test;

public class Solution {

    /**
     * 给定一个整数数组 nums 和一个目标值 target，请你在该数组中找出和为目标值的那 两个 整数，并返回他们的数组下标。
     * 你可以假设每种输入只会对应一个答案。但是，数组中同一个元素不能使用两遍。
     */
    public int[] twoSum(int[] nums, int target) {
        int[] result = new int[2];
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if ((nums[i] + nums[j]) == target) {
                    result[0] = i;
                    result[1] = j;
                    return result;
                }
            }
        }
        return result;
    }

    public int reverse(int x) {
        long result = (x >= 0 ? Long.parseLong(new StringBuilder(x + "").reverse().toString())
                : (0 - Long.parseLong(new StringBuilder((0 - x) + "").reverse().toString())));
        if (result > Integer.MAX_VALUE || result < Integer.MIN_VALUE) {
            return 0;
        }
        return (int)result;
    }

//    @Test
//    public void demo() {
//        var a = isPalindrome(12);
//        Object b = null;
//    }

}
