package org.jbackprop.ann;

import lombok.Getter;
import lombok.Setter;
import org.jbackprop.ann.lossfunctions.LossFunction;
import org.jbackprop.dataset.DataSet;
import org.jbackprop.dataset.Row;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class Network {
    private List<HiddenLayer> hiddenLayers;
    private OutputLayer outputLayer;
    private int currentEpoch;
    private double lossOfEpoch;
    private double lossOfPreviousEpoch;

    private int[] architecture;
    private double[] networkOutput;
    private LossFunction lossFunction;
    private NetworkBuilder networkBuilder;
    private DataSet dataSet;

    public void setNetworkBuilder(NetworkBuilder networkBuilder) {
        this.architecture = networkBuilder.getArchitecture();
        this.dataSet = networkBuilder.getDataSet();
        this.networkBuilder = networkBuilder;
        lossFunction = networkBuilder.getLossFunction();
    }

    public Network() {
    }

    public void beforeEpoch() {
    }

    public void afterEpoch() {

    }

    public void build() {
        hiddenLayers = new ArrayList<>();
        var connectionsPerNeuron = dataSet.getInputDimension();
        HiddenLayer previousLayer = null;

        int i;
        for (i = 0; i < architecture.length - 1; i++) {
            var hiddenLayer = new HiddenLayer();
            hiddenLayer.build(architecture[i],
                    connectionsPerNeuron, previousLayer, networkBuilder);

            connectionsPerNeuron = architecture[i];
            previousLayer = hiddenLayer;

            hiddenLayers.add(hiddenLayer);
        }

        outputLayer = new OutputLayer();
        outputLayer.build(architecture[i],
                connectionsPerNeuron, previousLayer, networkBuilder);
    }

    public void forwardPass(double[] firstInput) {
        double[] previousActivations = firstInput;

        for (HiddenLayer layer : hiddenLayers)
            previousActivations =
                    layer.applyActivations(previousActivations);

        networkOutput = outputLayer.applyActivations(previousActivations);
    }

    public void learn() {
        var epochs = networkBuilder.getEpochs();
        var desiredLoss = networkBuilder.getDesiredLoss();

        do {
            currentEpoch++;
            beforeEpoch();
            epoch(dataSet);
            afterEpoch();
            lossOfPreviousEpoch = lossOfEpoch;
            lossOfEpoch = 0;
            epochs--;
        }

        while (epochs > 0 &&
                desiredLoss < lossOfPreviousEpoch);
    }

    private void epoch(DataSet dataSet) {
        for (Row row : dataSet.getRows()) {
            forwardPass(row.getInputs());
            backwardPass(row.getTargets());
            lossOfEpoch += calculateLossOfIteration();
            adjustWeights();
        }
    }

    private void backwardPass(double[] targets) {
        outputLayer.calculateDeltas(targets);

        for (int i = hiddenLayers.size() - 1; i >= 0; i--) {
            hiddenLayers.get(i).calculateDeltas();
        }
    }

    private void adjustWeights() {
        outputLayer.adjustWeights();

        for (HiddenLayer layer : hiddenLayers) {
            layer.adjustWeights();
        }
    }

    private double calculateLossOfIteration() {
        return lossFunction.calculateLossOfIteration(outputLayer);
    }

    public double getLossOfEpoch() {
        return this.lossOfEpoch;
    }

    public NetworkBuilder getNetworkBuilder() {
        return this.networkBuilder;
    }
}
