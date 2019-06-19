package org.monarchinitiative.threes.core.scoring.scorers;

import com.google.common.collect.ImmutableMap;
import org.monarchinitiative.threes.core.model.SplicingTernate;
import org.monarchinitiative.threes.core.pwm.SplicingInformationContentAnnotator;
import org.monarchinitiative.threes.core.scoring.ScoringStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 *
 */
public class ScalingScorerFactory implements ScorerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawScoringFactory.class);

    private final RawScoringFactory rawScoringFactory;

    private final ImmutableMap<ScoringStrategy, UnaryOperator<Double>> scalerMap;

    public ScalingScorerFactory(SplicingInformationContentAnnotator annotator) {
        this.rawScoringFactory = new RawScoringFactory(annotator);
        this.scalerMap = ImmutableMap.<ScoringStrategy, UnaryOperator<Double>>builder()
                .put(ScoringStrategy.CANONICAL_DONOR, sigmoidScaler(0.29, -1))
                .put(ScoringStrategy.CRYPTIC_DONOR, sigmoidScaler(-5.52, -1))
                .put(ScoringStrategy.CRYPTIC_DONOR_IN_CANONICAL_POSITION, sigmoidScaler(-4.56, -1))
                .put(ScoringStrategy.CANONICAL_ACCEPTOR, sigmoidScaler(-1.50, -1))
                .put(ScoringStrategy.CRYPTIC_ACCEPTOR, sigmoidScaler(-8.24, -1))
                .put(ScoringStrategy.CRYPTIC_ACCEPTOR_IN_CANONICAL_POSITION, sigmoidScaler(-4.59, -1))
                .build();
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

    private static UnaryOperator<Double> sigmoidScaler(double threshold, double steepness) {
        return d -> sigmoid(d, steepness, threshold);
    }

    @Override
    public Function<SplicingTernate, Double> scorerForStrategy(ScoringStrategy strategy) {
        return rawScoringFactory.scorerForStrategy(strategy).andThen(scalerMap.get(strategy));
    }

}
