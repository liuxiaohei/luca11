import org.junit.jupiter.api.Test;

public class Solution {

    public static int demo(int[] nums, int from, int to) {
        if (to - from == 0) return from;
        if (to - from == 1) return nums[from] > nums[to] ? from : to;
        int half = (to + from) / 2;
        return nums[half] > nums[half - 1] ? demo(nums, half, to) : demo(nums, from, half - 1);
    }

    @Test
    public void main() {
        int [] a = new int[]{1,3,5,7,6,4};
        int b = demo(a,0,a.length -1);
        System.out.println(b);
    }
}
