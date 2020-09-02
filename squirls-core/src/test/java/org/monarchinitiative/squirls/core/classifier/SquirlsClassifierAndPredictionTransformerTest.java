package org.monarchinitiative.squirls.core.classifier;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.squirls.core.SimpleClassifiable;
import org.monarchinitiative.squirls.core.TestDataSourceConfig;
import org.monarchinitiative.squirls.core.classifier.transform.prediction.PredictionTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

/**
 * This test uses real data to test that combination of {@link SquirlsClassifier} and {@link PredictionTransformer}
 * yields a correct prediction.
 * <p>
 * Prediction values are derived from model training in Python. This test ensures that both Python and Java
 * implementations work in the same way.
 */
@SpringBootTest(classes = {TestDataSourceConfig.class})
public class SquirlsClassifierAndPredictionTransformerTest {

    private static final double EPSILON = 5E-7;

    @Autowired
    public SquirlsClassifier classifier;

    @Autowired
    public PredictionTransformer transformer;

    @ParameterizedTest
    @CsvSource({
            "2,9.94544383637791,3.74693975339561,213,0,-9.96100606583484,7.98699998855591,2.1592859,1.9968,.998501",
            "-206,0,-12.0854782243879,-2,9.9614496943982,0,9.04100036621094,1.9790803,0.5168,.86723035",
            "-21,0,-10.3352129705513,104,0,-17.3667340869353,0.811999976634979,2.7493869,1.2138,.00770952",
            "-144,0,-6.07127393937876,-4,0.213403638109193,0,-1.52300000190735,0.5297559,0.2235,.00951794",
            "-175.,0.,-9.268881,-8.,2.160647,0.,3.428,-1.213167,-1.5895,.958987" // chr3-165504107-A-C
    })
    public void predictAndTransform(double donorOffset, double canonicalDonor, double crypticDonor,
                                    double acceptorOffset, double canonicalAcceptor, double crypticAcceptor,
                                    double phylop, double hexamer, double septamer,
                                    double expectedPathogenicity) {
        final SimpleClassifiable instance = new SimpleClassifiable(Map.of(
                "donor_offset", donorOffset,
                "canonical_donor", canonicalDonor,
                "cryptic_donor", crypticDonor,

                "acceptor_offset", acceptorOffset,
                "canonical_acceptor", canonicalAcceptor,
                "cryptic_acceptor", crypticAcceptor,

                "phylop", phylop,
                "hexamer", hexamer,
                "septamer", septamer));

        final SimpleClassifiable score = classifier.predict(instance);
        final SimpleClassifiable transformed = transformer.transform(score);

        assertThat(expectedPathogenicity, is(closeTo(transformed.getPrediction().getMaxPathogenicity(), EPSILON)));
    }

}