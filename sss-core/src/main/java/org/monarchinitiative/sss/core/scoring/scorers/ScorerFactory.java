package org.monarchinitiative.sss.core.scoring.scorers;

import org.monarchinitiative.sss.core.scoring.ScoringStrategy;

public interface ScorerFactory {

    SplicingScorer scorerForStrategy(ScoringStrategy strategy);
}
