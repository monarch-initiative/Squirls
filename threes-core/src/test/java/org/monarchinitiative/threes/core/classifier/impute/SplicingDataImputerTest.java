package org.monarchinitiative.threes.core.classifier.impute;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.classifier.FeatureData;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

class SplicingDataImputerTest {

    private SplicingDataImputer splicingDataImputer;

    @BeforeEach
    void setUp() {
        splicingDataImputer = new SplicingDataImputer(Map.of("a", .5, "b", 1.5));
    }

    @Test
    void transform() throws Exception {
        FeatureData imputed = splicingDataImputer.transform(FeatureData.builder()
                .addFeature("a", 1.)
                .addFeature("b", Double.NaN)
                .build());
        FeatureData expected = FeatureData.builder().addFeature("a", 1.).addFeature("b", 1.5).build();
        assertThat(imputed, is(expected));

        imputed = splicingDataImputer.transform(FeatureData.builder()
                .addFeature("a", 1.)
                .addFeature("b", Double.NaN)
                .build());
        expected = FeatureData.builder().addFeature("a", 1.).addFeature("b", 1.5).build();
        assertThat(imputed, is(expected));
    }

    @Test
    void getSupportedFeatureNames() {
        assertThat(splicingDataImputer.usedFeatureNames(), hasItems("a", "b"));
    }
}