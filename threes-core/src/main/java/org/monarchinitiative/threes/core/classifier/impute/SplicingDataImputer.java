package org.monarchinitiative.threes.core.classifier.impute;

import org.monarchinitiative.threes.core.classifier.FeatureData;
import org.monarchinitiative.threes.core.classifier.FeatureTransformer;
import org.monarchinitiative.threes.core.classifier.PredictionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SplicingDataImputer implements FeatureTransformer<FeatureData> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SplicingDataImputer.class);

    private final Map<String, Double> medianMap;

    public SplicingDataImputer(Map<String, Double> medianMap) {
        this.medianMap = medianMap;
    }

    public SplicingDataImputer(List<String> featureNames, List<Double> featureStatistics) {
        if (featureNames.size() != featureStatistics.size()) {
            throw new IllegalArgumentException(String.format("# feature names (%d) must match # feature statistics (%d)",
                    featureNames.size(), featureStatistics.size()));
        }
        this.medianMap = new HashMap<>();
        for (int i = 0; i < featureNames.size(); i++) {
            medianMap.put(featureNames.get(i), featureStatistics.get(i));
        }
    }

    @Override
    public Set<String> usedFeatureNames() {
        return medianMap.keySet();
    }

    @Override
    public FeatureData transform(FeatureData fd) throws PredictionException {
        if (!fd.getFeatureNames().containsAll(usedFeatureNames())) {
            // instance does not contain all the required features
            String msg = String.format("Missing at least one required feature. Required: `%s`, Provided: `%s`",
                    usedFeatureNames().stream().collect(Collectors.joining(",", "[", "]")),
                    fd.getFeatureNames().stream().collect(Collectors.joining(",", "[", "]")));
            LOGGER.warn(msg);
            throw new PredictionException(msg);
        }
        final FeatureData.Builder<FeatureData> builder = fd.toBuilder();

        for (String featureName : fd.getFeatureNames()) {
            final Double value = fd.getFeature(featureName, Double.class);
            builder.addFeature(featureName, value.isNaN() ? medianMap.get(featureName) : value);
        }

        return builder.build();
    }
}
