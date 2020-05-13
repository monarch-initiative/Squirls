package org.monarchinitiative.threes.core.classifier;

import org.jblas.DoubleMatrix;
import org.monarchinitiative.threes.core.classifier.forest.RandomForest;

/**
 * This classifier consists of two {@link RandomForest} classifiers which are used to make a prediction for each
 * {@link FeatureData} instance.
 */
public class SimpleOverlordClassifier implements OverlordClassifier {

    private final RandomForest<FeatureData> donorClf, acceptorClf;

    private final double donorThreshold, acceptorThreshold;

    public SimpleOverlordClassifier(RandomForest<FeatureData> donorClf,
                                    RandomForest<FeatureData> acceptorClf,
                                    double donorThreshold,
                                    double acceptorThreshold) {
        // TODO - make with builder?
        this.donorClf = donorClf;
        this.acceptorClf = acceptorClf;
        this.donorThreshold = donorThreshold;
        this.acceptorThreshold = acceptorThreshold;
    }

    @Override
    public int predict(FeatureData instance) {
        final DoubleMatrix donorProba = donorClf.predictProba(instance);
        final DoubleMatrix acceptorProba = acceptorClf.predictProba(instance);

        final int isDonorPathogenic = donorProba.get(0, 1) < donorThreshold ? 0 : 1;
        final int isAcceptorPathogenic = acceptorProba.get(0, 1) < acceptorThreshold ? 0 : 1;

        return Math.max(isDonorPathogenic, isAcceptorPathogenic);
    }

    @Override
    public DoubleMatrix predictProba(FeatureData instance) {
        final DoubleMatrix donorProba = donorClf.predictProba(instance);
        final DoubleMatrix acceptorProba = acceptorClf.predictProba(instance);

        final double pathoProba = Math.max(donorProba.get(0, 1), acceptorProba.get(0, 1));

        return new DoubleMatrix(1, 2, 1 - pathoProba, pathoProba);
    }
}
