package org.monarchinitiative.threes.core.classifier.impute;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.classifier.FeatureData;
import org.monarchinitiative.threes.core.classifier.SimpleFeatureData;

import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

class MedianImputerTest {

    private MedianImputer imputer;

    @BeforeEach
    void setUp() {
        imputer = new MedianImputer(Map.of("a", .5, "b", 1.5)) {
            @Override
            public Set<String> getSupportedFeatureNames() {
                return Set.of("a", "b");
            }
        };
    }

    @Test
    void transform() {
        FeatureData imputed = imputer.transform().apply(SimpleFeatureData.of(Map.of("a", 1., "b", Double.NaN)));
        assertThat(imputed, is(SimpleFeatureData.of(Map.of("a", 1., "b", 1.5))));

        imputed = imputer.transform().apply(SimpleFeatureData.of(Map.of("a", Double.NaN, "b", 3.5)));
        assertThat(imputed, is(SimpleFeatureData.of(Map.of("a", .5, "b", 3.5))));
    }

    @Test
    void getSupportedFeatureNames() {
        assertThat(imputer.getSupportedFeatureNames(), hasItems("a", "b"));
    }
}