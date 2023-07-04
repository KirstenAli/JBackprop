package org.jbackprop.ann.convolution;

public class MatrixFilter {

    public static double[][] applyFilter(double[][] matrix, double[][] filter, int stride) {
        int filterSize = filter.length;

        Convolution applyFilter = (i, j)-> {
            final double[] sum = {0};

            WindowOperation windowOperation = (row, col)->
                    sum[0] += matrix[i*stride+row][j*stride+col] * filter[row][col];

            Window.apply(0, 0, filterSize, filterSize, windowOperation);

            return sum[0];
        };

        return SlidingWindow.convolve(matrix, filterSize, stride, applyFilter);
    }

    public static void main(String[] args){
        double[][] matrix1 = {
                {1, 2, 3, 1, 2, 3},
                {4, 5, 6, 1, 2, 3},
                {7, 8, 9, 1, 2, 3},
                {7, 8, 9, 1, 2, 3}
        };
        double[][] filter1 = {
                {1, 0},
                {0, 1},

        };
        int stride1 = 1;

        double[][] filteredMatrix1 = applyFilter(matrix1, filter1, stride1);

// Print the resulting filtered matrix
        for (int i = 0; i < filteredMatrix1.length; i++) {
            for (int j = 0; j < filteredMatrix1[0].length; j++) {
                System.out.print(filteredMatrix1[i][j] + " ");
            }
            System.out.println();
        }

    }

}
