//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.clientservernn.server.neuralNetwork;

import com.clientservernn.server.utilities.RawCharData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;



/**
 * The class {@code Network} represents neural network which operate
 * any type object.
 * Implementation borrowed from David Kopec:
 * <a href="https://github.com/davecom/ClassicComputerScienceProblemsInJava">
 *     https://github.com/davecom/ClassicComputerScienceProblemsInJava</a>
 * @param <T> the type of objects operated by the {@code Network}
 *
 * @author David Kopec
 * @author Yauheni Slabko
 * @since 1.0
 */
public class Network<T> {
    private final List<Layer> layers = new ArrayList<>();
    public Network(int[] layerStructure, double learningRate, DoubleUnaryOperator activationFunction, DoubleUnaryOperator derivativeActivationFunction) {
        Layer inputLayer = new Layer(Optional.empty(), layerStructure[0], learningRate, activationFunction, derivativeActivationFunction);
        this.layers.add(inputLayer);
        for(int i = 1; i < layerStructure.length; ++i) {
            Layer nextLayer = new Layer(Optional.of(this.layers.get(i - 1)), layerStructure[i], learningRate, activationFunction, derivativeActivationFunction);
            this.layers.add(nextLayer);
        }

    }

    private double[] getOutputs(double[] input) {
        double[] result = input;
        Layer layer;
        for (Layer value : this.layers) {
            layer = value;
            result = layer.outputs(result);
        }
        return result;
    }

    private void backpropagate(double[] expected) {
        int lastLayer = this.layers.size() - 1;
        (this.layers.get(lastLayer)).calculateDeltasForOutputLayer(expected);

        for(int i = lastLayer - 1; i >= 0; --i) {
            (this.layers.get(i)).calculateDeltasForHiddenLayer(this.layers.get(i + 1));
        }

    }

    private void updateWeights() {
        for (Layer layer : this.layers.subList(1, this.layers.size())) {
            for (Neuron neuron : layer.neurons) {
                for (int w = 0; w < neuron.weights.length; ++w) {
                    neuron.weights[w] += neuron.learningRate * ((Layer) layer.previousLayer.get()).outputCache[w] * neuron.delta;
                }
            }
        }

    }

    public void train(List<double[]> inputs, List<double[]> expects) {
        for(int i = 0; i < inputs.size(); ++i) {
            double[] xs = (double[])inputs.get(i);
            double[] ys = (double[])expects.get(i);
            this.getOutputs(xs);
            this.backpropagate(ys);
            this.updateWeights();
        }

    }

    public HashMap<T, Double> getCheck(byte[] imageData, Function<double[], HashMap<T, Double>> interpret) {
        double[] result = this.getOutputs(RawCharData.getDoublesArrayRefactored(imageData));
        return interpret.apply(result);
    }
}
