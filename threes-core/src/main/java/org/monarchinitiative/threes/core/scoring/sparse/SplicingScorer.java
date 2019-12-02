package org.monarchinitiative.threes.core.scoring.sparse;

import org.monarchinitiative.threes.core.model.SplicingTernate;

import java.util.function.Function;

public interface SplicingScorer {

    Function<SplicingTernate, Double> scoringFunction();

    default double score(SplicingTernate ternate) {
        return scoringFunction().apply(ternate);
    }
}
