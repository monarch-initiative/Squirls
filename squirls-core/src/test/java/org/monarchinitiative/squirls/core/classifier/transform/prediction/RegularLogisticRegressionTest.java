package org.monarchinitiative.squirls.core.classifier.transform.prediction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.squirls.core.Prediction;
import org.monarchinitiative.squirls.core.classifier.StandardPrediction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RegularLogisticRegressionTest {

    private static final double EPSILON = 5E-8;

    private static final double DONOR_SLOPE = 10.58023823, ACCEPTOR_SLOPE = 18.25294229;

    private static final double INTERCEPT = -5.07846126;

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
            "0.,0.,.00619092",
            ".3,.3,.97265492",
            ".5,.5,.999912",
            ".7,.7,.99999972",
            "1.,1.,1."})
    public void transformSpan(double donor, double acceptor, double expectedProba) {
        double threshold = .4;
        double expectedThreshold = 0.9984295;

        final MutablePrediction mutablePrediction = new SimpleMutablePrediction();
        final StandardPrediction sp = StandardPrediction.of(
                Prediction.PartialPrediction.of("donor", donor, threshold),
                Prediction.PartialPrediction.of("acceptor", acceptor, threshold));
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