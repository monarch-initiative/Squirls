package org.monarchinitiative.squirls.cli.visualization;

import java.util.Map;

public interface VisualizationContextSelector {

    /**
     * Decide which context is the best for visualization of this prediction.
     *
     * @param features map with splicing features
     * @return context
     * @throws MissingFeatureException when a feature that is required to decide about the context is not present
     */
    VisualizationContext selectContext(Map<String, Double> features) throws MissingFeatureException;
}
