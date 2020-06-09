package org.monarchinitiative.squirls.core.classifier;

import java.util.Set;

public interface SquirlsClassifier {

    /**
     * @return set with expected feature names
     */
    Set<String> usedFeatureNames();

    <T extends Classifiable> T predict(T data);

}
