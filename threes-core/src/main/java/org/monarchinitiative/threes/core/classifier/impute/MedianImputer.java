package org.monarchinitiative.threes.core.classifier.impute;

import org.monarchinitiative.threes.core.classifier.FeatureData;
import org.monarchinitiative.threes.core.classifier.FeatureTransformer;
import org.monarchinitiative.threes.core.classifier.SimpleFeatureData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public abstract class MedianImputer implements FeatureTransformer<FeatureData> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MedianImputer.class);

    private final Map<String, Double> medianMap;

    public MedianImputer(Map<String, Double> medianMap) {
        this.medianMap = medianMap;
    }

    @Override
    public UnaryOperator<FeatureData> transform() {
        return fd -> {
            if (!fd.getFeatureNames().containsAll(getSupportedFeatureNames())) {
                // instance does not contain all the required features
                String msg = String.format("Missing at least one required feature. Required: `%s`, Provided: `%s`",
                        getSupportedFeatureNames().stream().collect(Collectors.joining(",", "[", "]")),
                        fd.getFeatureNames().stream().collect(Collectors.joining(",", "[", "]")));
                LOGGER.warn(msg);
                throw new RuntimeException(msg);
            }
            final Map<String, Object> imputed = new HashMap<>();

            for (String featureName : fd.getFeatureNames()) {
                @SuppressWarnings("OptionalGetWithoutIsPresent") // we check above
                final Double value = fd.getFeature(featureName, Double.class).get();
                imputed.put(featureName, value.isNaN() ? medianMap.get(featureName) : value);
            }

            return SimpleFeatureData.of(imputed);
        };
    }
}
