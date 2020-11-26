package org.ld;

import java.util.stream.IntStream;

/**
 * 螺旋打印矩阵
 */
public class SpiralMatrix {

    public int[][] generateMatrix(int m,int n) {
        int[][] res = new int[m][n];
        final int total = m * n;
        int num = 1;
        int rowBegin = 0;
        int rowEnd = m - 1;
        int colBegin = 0;
        int colEnd = n - 1;
        while (num <= total) {
            for (int y = colBegin; y <= colEnd; y++)
                res[rowBegin][y] = num++;
            rowBegin++;
            for (int x = rowBegin; x <= rowEnd; x++)
                res[x][colEnd] = num++;
            colEnd--;
            for (int y = colEnd; y >= colBegin; y--)
                res[rowEnd][y] = num++;
            rowEnd--;
            for (int x = rowEnd; x >= rowBegin; x--)
                res[x][colBegin] = num++;
            colBegin++;
        }
        return res;
    }

    /**
     * 测试
     */
    public static void main(String[] args) {
        int m = 6; // 多少行
        int n = 4; // 多少列
        SpiralMatrix s = new SpiralMatrix();
        int[][] data = s.generateMatrix(m,n);
        IntStream.range(0,m)
                .peek(e -> System.out.println())
                .forEach(i -> IntStream.range(0,n)
                        .forEach(j -> System.out.print(data[i][j] + "\t")));
    }
}
