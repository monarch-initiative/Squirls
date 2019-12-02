package org.monarchinitiative.threes.core.scoring.sparse;

import com.google.common.collect.ImmutableMap;
import org.monarchinitiative.threes.core.model.SplicingTernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 *
 */
public class ScalingScorerFactory implements ScorerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawScorerFactory.class);

    private final RawScorerFactory rawScorerFactory;

    private final ImmutableMap<ScoringStrategy, UnaryOperator<Double>> scalerMap;

    public ScalingScorerFactory(RawScorerFactory rawScorerFactory, Map<ScoringStrategy, UnaryOperator<Double>> scalerMap) {
        this.rawScorerFactory = rawScorerFactory;
        this.scalerMap = ImmutableMap.copyOf(scalerMap);
    }

    @Override
    public Function<SplicingTernate, Double> scorerForStrategy(ScoringStrategy strategy) {
        return rawScorerFactory.scorerForStrategy(strategy).andThen(scalerMap.get(strategy));
    }

}
