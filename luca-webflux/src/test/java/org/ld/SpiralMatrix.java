package org.ld;

import java.util.stream.Stream;

public class SpiralMatrix {

    enum Direction {
        right(1,2),
        down(2,3),
        left(3,4),
        up(4,1);

        Direction(Integer value, Integer nextValue) {
            this.value = value;
            this.nextValue = nextValue;
        }

        public Integer value;
        public Integer nextValue;

        public Direction getNextDirection() {
            return Stream.of(values()).filter(e -> e.nextValue.equals(this.value)).findAny().orElse(Direction.right);
        }
    }

    public int[][] createMatrix(int n) {
        int[][] matrix = new int[n][n];           //n*n的二维数组，初始元素值都为0
        Direction direction = Direction.right;
        int numb = n * n;
        int i = 0, j = 0;
        for (int p = 1; p <= numb; p++) {
            matrix[i][j] = p;
            if (direction.equals(Direction.right)) {
                if (j + 1 < n && matrix[i][j + 1] == 0) {
                    j++;
                } else {
                    i++;
                    direction = direction.getNextDirection();
                    continue;
                }
            }
            if (direction.equals(Direction.down)) {
                if (i + 1 < n && matrix[i + 1][j] == 0) {
                    i++;
                } else {
                    j--;
                    direction = direction.getNextDirection();
                    continue;
                }
            }
            if (direction.equals(Direction.left)) {
                if (j - 1 >= 0 && matrix[i][j - 1] == 0) {
                    j--;
                } else {
                    i--;
                    direction = direction.getNextDirection();
                    continue;
                }
            }
            if (direction.equals(Direction.up)) {
                if (i - 1 >= 0 && matrix[i - 1][j] == 0) {
                    i--;
                } else {
                    j++;
                    direction = direction.getNextDirection();
                }
            }
        }
        return matrix;
    }

    /**
     * 测试
     */
    public static void main(String[] args) {
        int n = 4;
        SpiralMatrix s = new SpiralMatrix();
        int[][] data = s.createMatrix(n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                System.out.print(data[i][j] + " ");
            }
            System.out.println();
        }
    }
}
