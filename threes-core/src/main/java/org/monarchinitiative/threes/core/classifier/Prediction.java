package org.monarchinitiative.threes.core.classifier;

import java.util.Set;

public interface Prediction {
    Set<StandardPrediction.Fragment> getFragments();

    boolean isPathogenic();

    double getPathoProba();
}
