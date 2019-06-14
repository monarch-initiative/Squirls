package org.monarchinitiative.threes.core.scoring.scorers;

import org.monarchinitiative.threes.core.model.SplicingTernate;
import org.monarchinitiative.threes.core.scoring.ScoringStrategy;

import java.util.function.Function;

public interface ScorerFactory {

    Function<SplicingTernate, Double> scorerForStrategy(ScoringStrategy strategy);

    default Function<SplicingTernate, Double> nullScorer() {
        return t -> Double.NaN;
    }
}
