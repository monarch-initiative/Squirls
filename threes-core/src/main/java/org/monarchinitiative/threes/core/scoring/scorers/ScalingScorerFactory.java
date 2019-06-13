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
}
