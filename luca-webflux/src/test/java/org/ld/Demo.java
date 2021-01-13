package org.ld;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

/**
 * -- a-z分别对应数字1-26, 给你一串数字(不能改变顺序), 方法得出:一串数字解码成对应的英文字母, 有几种可能性?
 * <p>
 * 123
 * abc
 * <p>
 * 12 3
 * lc
 * <p>
 * 1 23
 * aw
 * 3
 */
public class Demo {
    public static void main(String... args) {
        System.out.println(demo(List.of(1, 2, 3, 1, 2, 3)));
    }

    public static int demo(List<Integer> list) {
        if (list.size() == 0) {
            return 0;
        }
        if (list.size() == 1) {
            return 1;
        }
        if (list.size() == 2) {
            if (list.get(1) == 0)
                return 1;
            if (list.get(0) == 1)
                return 2;
            if (list.get(0) == 2 && (list.get(1) >= 1 && list.get(1) <= 6)) {
                return 2;
            }
            return 1;
        }
        if (list.get(0) == 1) {
            if (list.get(1) == 0) {
                return demo(list.stream().skip(2).collect(Collectors.toList()));
            } else {
                return demo(list.stream().skip(2).collect(Collectors.toList()))
                        + demo(list.stream().skip(1).collect(Collectors.toList()));
            }
        }
        if (list.get(0) == 2) {
            if (list.get(1) == 0) {
                return demo(list.stream().skip(2).collect(Collectors.toList()));
            } else if (list.get(1) >= 1 && list.get(1) <= 6) {
                return demo(list.stream().skip(2).collect(Collectors.toList()))
                        + demo(list.stream().skip(1).collect(Collectors.toList()));
            } else {
                return demo(list.stream().skip(1).collect(Collectors.toList()));
            }
        }
        return demo(list.stream().skip(1).collect(Collectors.toList()));
    }

    @Test
    public void demo() {
        System.out.println(exist(
                new char[][]
                        {
                                {'A', 'B', 'C', 'E'},
                                {'S', 'F', 'C', 'S'},
                                {'A', 'D', 'E', 'E'}
                        }
                , "ABCCED"));
    }

    public boolean exist(char[][] board, String word) {
        if (board == null || board.length == 0) return false;
        int m = board.length;
        int n = board[0].length;
        boolean[] flag = new boolean[m * n];
        //从每个位置开始遍历看是否包含此字符串。
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++) {
                if (helper(board, word, i, j, flag, 0)) return true;
            }
        return false;
    }

    public boolean helper(char[][] board, String word, int i, int j, boolean[] flag, int index) {
        if (index == word.length()) return true;
        if (i < 0
                || i >= board.length
                || j < 0
                || j >= board[0].length
                || board[i][j] != word.charAt(index)
                || flag[i * board[0].length + j]
        )
            return false;
        flag[i * board[0].length + j] = true;
        if (helper(board, word, i + 1, j, flag, index + 1)
                || helper(board, word, i - 1, j, flag, index + 1)
                || helper(board, word, i, j - 1, flag, index + 1)
                || helper(board, word, i, j + 1, flag, index + 1))
            return true;
        flag[i * board[0].length + j] = false;
        return false;
    }



}
