package org.monarchinitiative.squirls.core.classifier.transform.prediction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.squirls.core.classifier.PartialPrediction;
import org.monarchinitiative.squirls.core.classifier.Prediction;
import org.monarchinitiative.squirls.core.classifier.StandardPrediction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Deprecated
public class SimpleLogisticRegressionTest {

    private static final double EPSILON = 5E-6;

    /*
     * Slope and intercept are real values from v1.1 classifier.
     */
    private static final double SLOPE = 13.64842177;

    private static final double INTERCEPT = -4.90967636;

    private SimpleLogisticRegression transformer;

    @BeforeEach
    void setUp() {
        transformer = SimpleLogisticRegression.getInstance(SLOPE, INTERCEPT);
    }

    /**
     * Validate that {@link SimpleLogisticRegression} predicts the same values as scikit-learn's
     * LogisticRegression classifier with the same slope and intercept.
     */
    @ParameterizedTest
    @CsvSource({
            "0.,.00732088",
            ".3,.30679419",
            ".5,.87152772",
            ".7,.99047457",
            "1.,.99983977"})
    void transformSpan(double proba, double expectedProba) {
        double threshold = .5;
        double expectedThreshold = 0.871527;

        MutablePrediction mp = new SimpleMutablePrediction();
        mp.setPrediction(StandardPrediction.of(PartialPrediction.of("bla", proba, threshold)));

        Prediction transformed = transformer.transform(mp).getPrediction();
        assertThat(transformed.getPartialPredictions(), hasSize(1));

        @SuppressWarnings("OptionalGetWithoutIsPresent") final PartialPrediction partial = transformed.getPartialPredictions().stream().findFirst().get();
        assertThat(partial.getPathoProba(), is(closeTo(expectedProba, EPSILON)));
        assertThat(partial.getThreshold(), is(closeTo(expectedThreshold, EPSILON)));
    }

}