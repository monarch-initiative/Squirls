package org.monarchinitiative.squirls.core.classifier.io;

import java.util.List;

/**
 * We expect to get an object with the following properties
 */
public interface OverallModelData {

    double getDonorThreshold();

    double getAcceptorThreshold();

    PipelineTransferModel getDonorClf();

    PipelineTransferModel getAcceptorClf();

    List<List<Double>> getSlope();

    List<Double> getIntercept();

    default PredictionTransformationParameters getLogisticRegressionParameters() {
        return new PredictionTransformationParameters(getSlope(), getIntercept());
    }
}
