package org.monarchinitiative.squirls.core.classifier.impute;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.classifier.transform.feature.MutableFeature;
import org.monarchinitiative.squirls.core.classifier.transform.feature.SplicingDataImputer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SplicingDataImputerTest {

    private SplicingDataImputer<MutableFeature> splicingDataImputer;

    @BeforeEach
    public void setUp() {
        splicingDataImputer = new SplicingDataImputer<>(Map.of("a", .5, "b", 1.5));
    }

    @Test
    public void transform() throws Exception {
        MutableFeature feature = new SimpleMutableFeature(new HashMap<>(Map.of(
                "a", 1.,
                "b", Double.NaN,
                "c", Double.NaN)));
        MutableFeature imputed = splicingDataImputer.transform(feature);
        assertThat(imputed.getFeature("b", Double.class), is(closeTo(1.5, .005)));
        assertThat(imputed.getFeature("c", Double.class), is(notANumber())); // unknown feature is not imputed
    }

    @Test
    public void getSupportedFeatureNames() {
        assertThat(splicingDataImputer.usedFeatureNames(), hasItems("a", "b"));
    }

    private static class SimpleMutableFeature implements MutableFeature {

        private final Map<String, Object> featureMap;

        private SimpleMutableFeature(Map<String, Object> featureMap) {
            this.featureMap = featureMap;
        }

        @Override
        public Set<String> getFeatureNames() {
            return featureMap.keySet();
        }

        @Override
        public <T> T getFeature(String featureName, Class<T> clz) {
            return clz.cast(featureMap.get(featureName));
        }

        @Override
        public void putFeature(String name, Object value) {
            featureMap.put(name, value);
        }
    }
}