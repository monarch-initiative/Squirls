package org.monarchinitiative.squirls.core.classifier.transform.feature;

import org.monarchinitiative.squirls.core.classifier.PredictionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class SplicingDataImputer<T extends MutableFeature> implements FeatureTransformer<T> {

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
    public T transform(T fd) throws PredictionException {
        if (!fd.getFeatureNames().containsAll(usedFeatureNames())) {
            // instance does not contain all the required features
            String msg = String.format("Missing at least one required feature. Required: `%s`, Provided: `%s`",
                    usedFeatureNames().stream().collect(Collectors.joining(",", "[", "]")),
                    fd.getFeatureNames().stream().collect(Collectors.joining(",", "[", "]")));
            LOGGER.warn(msg);
            throw new PredictionException(msg);
        }

        for (String featureName : usedFeatureNames()) { // we only impute the features we know about
            final double value = fd.getFeature(featureName, Double.class);
            fd.putFeature(featureName, Double.isNaN(value) ? medianMap.get(featureName) : value);
        }

        return fd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SplicingDataImputer<?> that = (SplicingDataImputer<?>) o;
        return Objects.equals(medianMap, that.medianMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(medianMap);
    }

    @Override
    public String toString() {
        return "SplicingDataImputer{" +
                "medianMap=" + medianMap +
                '}';
    }
}
