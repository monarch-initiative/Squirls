package org.monarchinitiative.squirls.core.classifier.transform.prediction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.squirls.core.classifier.Constants;
import org.monarchinitiative.squirls.core.classifier.Prediction;
import org.monarchinitiative.squirls.core.classifier.StandardPrediction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RegularLogisticRegressionTest {

    private static final double EPSILON = 5E-8;

    /*
     Real parameters from the v0.4.4 model
     */
    private static final double DONOR_SLOPE = 10.580238234976031, ACCEPTOR_SLOPE = 18.252942293891184;

    private static final double INTERCEPT = -5.07846126079626;

    private RegularLogisticRegression transformer;

    @BeforeEach
    public void setUp() {
        transformer = RegularLogisticRegression.getInstance(DONOR_SLOPE, ACCEPTOR_SLOPE, INTERCEPT);
    }

    /**
     * Validate that {@link RegularLogisticRegression} predicts the same values as scikit-learn's
     * LogisticRegression classifier with the same slope and intercept.
     */
    @ParameterizedTest
    @CsvSource({
            "0.0,0.0,0.00619090",
            "0.0,0.3,0.59806797",
            "0.0,0.5,0.98284240",
            "0.0,0.7,0.99954670",
            "0.1,0.0,0.01762876",
            "0.5,0.0,0.55271780",
            "0.7,0.0,0.91114575",
            "1.0,1.0,1.0"
    })
    public void transformSpan(double donor, double acceptor, double expectedProba) {
        // real thresholds from the v0.4.4 model
        double donorThreshold = .051658395087546785;
        double acceptorThreshold = .012734158718713364;
        double expectedThreshold = .01339395;

        final MutablePrediction mutablePrediction = new SimpleMutablePrediction();
        final StandardPrediction sp = StandardPrediction.of(
                Prediction.PartialPrediction.of(Constants.DONOR_PIPE_NAME, donor, donorThreshold),
                Prediction.PartialPrediction.of(Constants.ACCEPTOR_PIPE_NAME, acceptor, acceptorThreshold));
        mutablePrediction.setPrediction(sp);
        final MutablePrediction transformed = transformer.transform(mutablePrediction);

        @SuppressWarnings("OptionalGetWithoutIsPresent") final Prediction.PartialPrediction partial = transformed.getPrediction().getPartialPredictions().stream().findFirst().get();
        assertThat(partial.getPathoProba(), is(closeTo(expectedProba, EPSILON)));
        assertThat(partial.getThreshold(), is(closeTo(expectedThreshold, EPSILON)));

        assertThat(mutablePrediction.getPrediction(), is(not(equalTo(sp))));
    }

    @ParameterizedTest
    @CsvSource({
            "donor,bla",
            "bla,acceptor"
    })
    public void predictionWithMissingProbaThresholdIsNotTransformed(String one, String two) {
        final MutablePrediction mutablePrediction = new SimpleMutablePrediction();
        final StandardPrediction prediction = StandardPrediction.of(
                Prediction.PartialPrediction.of(one, .5, .5),
                Prediction.PartialPrediction.of(two, .6, .6));
        mutablePrediction.setPrediction(prediction);

        final MutablePrediction transformed = transformer.transform(mutablePrediction);
        assertThat(transformed.getPrediction(), is(theInstance(prediction)));
    }
}