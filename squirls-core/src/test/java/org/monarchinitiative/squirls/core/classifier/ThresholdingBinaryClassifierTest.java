package org.monarchinitiative.squirls.core.classifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.squirls.core.SimpleClassifiable;
import org.monarchinitiative.squirls.core.TestDataSourceConfig;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = TestDataSourceConfig.class)
public class ThresholdingBinaryClassifierTest {


    private final double threshold = .9;
    @Mock
    private BinaryClassifier<Classifiable> classifier;
    private ThresholdingBinaryClassifier<Classifiable> instance;


    @BeforeEach
    public void setUp() {
        instance = ThresholdingBinaryClassifier.of(classifier, threshold);
    }

    @Test
    public void runPrediction() throws Exception {
        final SimpleClassifiable data = new SimpleClassifiable(Map.of());
        final String clfName = "Johnny";
        when(classifier.getName()).thenReturn(clfName);
        when(classifier.predictProba(data)).thenReturn(.5);

        final PartialPrediction prediction = instance.runPrediction(data);

        assertThat(prediction.getName(), is(clfName));
        assertThat(prediction.getPathoProba(), is(.5));
        assertThat(prediction.getThreshold(), is(threshold));
    }

    @Test
    public void usedFeatureNames() {
        final Set<String> featureNames = Set.of("a", "b", "c");
        when(classifier.usedFeatureNames()).thenReturn(featureNames);
        assertThat(instance.usedFeatureNames(), is(featureNames));
    }

    @Test
    public void throwExceptionWhenThresholdIsNan() {
        final Exception ex = assertThrows(IllegalArgumentException.class, () -> ThresholdingBinaryClassifier.of(classifier, Double.NaN));

        assertThat(ex.getMessage(), is("Threshold cannot be NaN"));
    }

    @Test
    public void throwExceptionWhenClassifierIsNull() {
        final Exception ex = assertThrows(NullPointerException.class, () -> ThresholdingBinaryClassifier.of(null, threshold));

        assertThat(ex.getMessage(), is("Classifier cannot be null"));
    }
}