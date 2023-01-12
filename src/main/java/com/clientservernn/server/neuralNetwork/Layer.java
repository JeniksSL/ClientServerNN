package com.clientservernn.server.neuralNetwork;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;


/**
 * The class {@code Layer} represents one layer of neural network.
 * Implementation borrowed from David Kopec:
 * <a href="https://github.com/davecom/ClassicComputerScienceProblemsInJava">
 *     https://github.com/davecom/ClassicComputerScienceProblemsInJava</a>
 *
 * @author David Kopec
 * @since 1.0
 */
public class Layer {
    public Optional<Layer> previousLayer;
    public List<Neuron> neurons = new ArrayList<>();
    public double[] outputCache;

    public Layer(Optional<Layer> previousLayer, int numNeurons, double learningRate, DoubleUnaryOperator activationFunction, DoubleUnaryOperator derivativeActivationFunction) {
        this.previousLayer = previousLayer;
        Random random = new Random();
        for(int i = 0; i < numNeurons; ++i) {
            double[] randomWeights = null;
            if (previousLayer.isPresent()) {
                randomWeights = random.doubles((previousLayer.get()).neurons.size()).toArray();
            }
            Neuron neuron = new Neuron(randomWeights, learningRate, activationFunction, derivativeActivationFunction);
            this.neurons.add(neuron);
        }

        this.outputCache = new double[numNeurons];
    }

    public double[] outputs(double[] inputs) {
        if (this.previousLayer.isPresent()) {
            this.outputCache = this.neurons.stream().mapToDouble((n) -> n.output(inputs)).toArray();
        } else {
            this.outputCache = inputs;
        }
        return this.outputCache;
    }

    public void calculateDeltasForOutputLayer(double[] expected) {
        for(int n = 0; n < this.neurons.size(); ++n) {
            (this.neurons.get(n)).delta = (this.neurons.get(n)).derivativeActivationFunction.applyAsDouble((this.neurons.get(n)).outputCache) * (expected[n] - this.outputCache[n]);
        }
    }

    public void calculateDeltasForHiddenLayer(Layer nextLayer) {
        for (int i = 0; i < neurons.size(); i++) {
            int index = i;
            double[] nextWeights = nextLayer.neurons.stream().mapToDouble(n -> n.weights[index]).toArray();
            double[] nextDeltas = nextLayer.neurons.stream().mapToDouble(n -> n.delta).toArray();
            double sumWeightsAndDeltas = Util.dotProduct(nextWeights, nextDeltas);
            neurons.get(i).delta = neurons.get(i).derivativeActivationFunction
                    .applyAsDouble(neurons.get(i).outputCache) * sumWeightsAndDeltas;
        }
    }
}
