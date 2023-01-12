
package com.clientservernn.server.neuralNetwork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The class {@code Util} contains service methods used in neural network.
 * Implementation borrowed from David Kopec:
 * <a href="https://github.com/davecom/ClassicComputerScienceProblemsInJava">
 *     https://github.com/davecom/ClassicComputerScienceProblemsInJava</a>
 * Method normalizeByFeatureScaling() modified taking into account
 * the format of analysing data.
 *
 *
 * @author David Kopec
 * @author Yauheni Slabko
 * @since 1.0
 */
public final class Util {
    public Util() {
    }

    public static double dotProduct(double[] xs, double[] ys) {
        double sum = 0.0;

        for(int i = 0; i < xs.length; ++i) {
            sum += xs[i] * ys[i];
        }
        return sum;
    }

    public static double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    public static double derivativeSigmoid(double x) {
        double sig = sigmoid(x);
        return sig * (1.0 - sig);
    }

    public static void normalizeByFeatureScaling(List<double[]> dataset) {
        for(int colNum = 0; colNum < (dataset.get(0)).length; ++colNum) {
            List<Double> column = new ArrayList<>();
            for (double[] row : dataset) {
                column.add(row[colNum]);
            }
            double maximum = Collections.max(column);
            double minimum = Collections.min(column);
            double difference = maximum - minimum;
            //Modified to exclude division by zero.
            difference = difference == 0.0 ? Double.MIN_VALUE : difference;
            double[] row;
            for (double[] doubles : dataset) {
                row = doubles;
                row[colNum] = (row[colNum] - minimum) / difference;
            }
        }

    }
}
