package org.monarchinitiative.threes.core.scoring.scorers;

import org.monarchinitiative.threes.core.pwm.SplicingInformationContentAnnotator;
import org.monarchinitiative.threes.core.scoring.ScoringStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ScalingScorerFactory implements ScorerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawScoringFactory.class);

    private final RawScoringFactory rawScoringFactory;


    public ScalingScorerFactory(SplicingInformationContentAnnotator annotator) {
        this.rawScoringFactory = new RawScoringFactory(annotator);

    }


    @Override
    public SplicingScorer scorerForStrategy(ScoringStrategy strategy) {
        SplicingScorer raw = (variant, region, sequenceInterval) ->
                rawScoringFactory.scorerForStrategy(strategy).score(variant, region, sequenceInterval);
        return raw;
    }


    /**
     * Apply <a href="https://en.wikipedia.org/wiki/Sigmoid_function">sigmoid</a> function to input variable
     * <code>x</code>.
     * <p>
     * Use default parameters:
     * <ul>
     * <li><b>steepness = -1</b></li>
     * <li><b>threshold = 0</b></li>
     * </ul>
     *
     * @param x input value
     * @return output in range (0, 1)
     */
    private static double sigmoid(double x) {
        return sigmoid(x, -1, 0);
    }


    /**
     * Apply <a href="https://en.wikipedia.org/wiki/Sigmoid_function">sigmoid</a> function to input variable
     * <code>x</code>.
     * <p>
     * Use parameters:
     *
     * @param x <b>value</b> value to be transformed
     * @param s <b>steepness</b> parameter - make sigmoid function more <em>threshold-like</em>
     * @param t <b>threshold</b> parameter - center the function on the <em>threshold</em> value
     * @return score
     */
    private static double sigmoid(double x, double s, double t) {
        return 1 / (1 + Math.exp(s * (x - t)));
    }

    /*
    .putSigmoidParameters("CANONICAL_DONOR", SigmoidParameters.of(0.29, -1))
                .putSigmoidParameters("CRYPTIC_DONOR", SigmoidParameters.of(-5.52, -1))
                .putSigmoidParameters("CRYPTIC_DONOR_FOR_DONOR_VARIANTS", SigmoidParameters.of(-4.56, -1))
                .putSigmoidParameters("CANONICAL_ACCEPTOR", SigmoidParameters.of(-1.50, -1))
                .putSigmoidParameters("CRYPTIC_ACCEPTOR", SigmoidParameters.of(-8.24, -1))
                .putSigmoidParameters("CRYPTIC_ACCEPTOR_FOR_ACCEPTOR_VARIANTS", SigmoidParameters.of(-4.59, -1))
     */
}
