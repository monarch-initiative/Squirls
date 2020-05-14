package org.monarchinitiative.threes.core.classifier;

import java.util.Set;
import java.util.function.UnaryOperator;

public interface FeatureTransformer<T extends FeatureData> {

    Set<String> getSupportedFeatureNames();

    UnaryOperator<T> transform();

}
