package org.monarchinitiative.threes.core.scoring.scorers;

import org.monarchinitiative.threes.core.model.SplicingTernate;

import java.util.function.Function;

public interface SplicingScorer {

    Function<SplicingTernate, Double> scoringFunction();
}
