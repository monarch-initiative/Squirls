package org.monarchinitiative.threes.core.scoring.scorers;

import org.monarchinitiative.threes.core.scoring.ScoringStrategy;

public interface ScorerFactory {

    SplicingScorer scorerForStrategy(ScoringStrategy strategy);
}
