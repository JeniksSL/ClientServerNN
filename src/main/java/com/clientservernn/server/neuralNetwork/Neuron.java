

package com.clientservernn.server.neuralNetwork;

import java.util.function.DoubleUnaryOperator;


/**
 * The class {@code Neuron} represents one neuron of neural network.
 * Implementation borrowed from David Kopec:
 * <a href="https://github.com/davecom/ClassicComputerScienceProblemsInJava">
 *     https://github.com/davecom/ClassicComputerScienceProblemsInJava</a>
 *
 * @author David Kopec
 * @since 1.0
 */
public class Neuron {
    public double[] weights;
    public final double learningRate;
    public double outputCache;
    public double delta;
    public final DoubleUnaryOperator activationFunction;
    public final DoubleUnaryOperator derivativeActivationFunction;

    public Neuron(double[] weights, double learningRate, DoubleUnaryOperator activationFunction, DoubleUnaryOperator derivativeActivationFunction) {
        this.weights = weights;
        this.learningRate = learningRate;
        this.outputCache = 0.0;
        this.delta = 0.0;
        this.activationFunction = activationFunction;
        this.derivativeActivationFunction = derivativeActivationFunction;
    }

    public double output(double[] inputs) {
        this.outputCache = Util.dotProduct(inputs, this.weights);
        return this.activationFunction.applyAsDouble(this.outputCache);
    }
}
