package org.jbackprop.ann;

import org.jbackprop.dataset.DataSet;
import org.jbackprop.dataset.Row;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter @Setter
public class Network{
    private List<Layer> layers;
    private Layer outputLayer;
    private double totalLoss;

    private final int[] neuronLayout;
    private List<Double> networkOutput;
    private LossFunction lossFunction;

    public Network(NetworkParams networkParams,
                   DataSet dataSet, int... neuronLayout){
        this.neuronLayout = neuronLayout;
        lossFunction = networkParams.getLossFunction();

        build(dataSet.getInputDimension(),
                networkParams);

        learn(dataSet,
                networkParams.getEpochs(),
                networkParams.getDesiredLoss());
    }

    public void beforeEpoch(){
    }
    public void afterEpoch(){
        
    }


    private void build(int inputDimension, NetworkParams networkParams){
        layers = new ArrayList<>();
        var numConnections = inputDimension;
        Layer previousLayer = null;

        for(int numNeurons: neuronLayout){
            var layer = new Layer();

            previousLayer = layer.build(numNeurons,
                    numConnections, previousLayer, networkParams);

            numConnections = numNeurons;

            layers.add(layer);
        }

        outputLayer = layers.get(layers.size()-1);
    }

    public void forwardPass(List<Double> firstInput){
        List<Double> previousActivations = firstInput;

        for(Layer layer: layers)
            previousActivations =
                    layer.calculateActivations(previousActivations);

        networkOutput = outputLayer.getActivations();
        System.out.println(networkOutput);
    }

    public void learn(DataSet dataSet,
                      int epochs,
                      double desiredLoss){
        do{
            beforeEpoch();
            epoch(dataSet);
            afterEpoch();
            totalLoss=0;
            epochs--;
        }

        while (epochs>0);
    }

    private void epoch(DataSet dataSet){
        for(Row row: dataSet.getRows()){
            forwardPass(row.getInput());
            totalLoss += calculateSumLoss();
            backwardPass(row.getTarget());
            adjustWeights();
        }
    }

    private void backwardPass(List<Double> targets){
        outputLayer.calculateDeltas(targets);

        for (int i=layers.size()-2; i>=0; i--){
            layers.get(i).calculateDeltas();
        }
    }

    private void adjustWeights(){
        for (Layer layer: layers){
            layer.adjustWeights();
        }
    }

    private double calculateSumLoss(){
        return lossFunction.calculateSumLoss(outputLayer);
    }
}