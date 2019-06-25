package org.monarchinitiative.threes.core.scoring.scorers;

import com.google.common.collect.ImmutableMap;
import org.monarchinitiative.threes.core.model.SplicingTernate;
import org.monarchinitiative.threes.core.pwm.SplicingInformationContentAnnotator;
import org.monarchinitiative.threes.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.threes.core.scoring.ScoringStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;


/**
 *
 */
public class RawScoringFactory implements ScorerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawScoringFactory.class);

    private final ImmutableMap<ScoringStrategy, SplicingScorer> scorerMap;

    public RawScoringFactory(SplicingInformationContentAnnotator annotator) {
        AlleleGenerator generator = new AlleleGenerator(annotator.getSplicingParameters());
        this.scorerMap = ImmutableMap.<ScoringStrategy, SplicingScorer>builder()
                .put(ScoringStrategy.CANONICAL_DONOR, new CanonicalDonorScorer(annotator, generator))
                .put(ScoringStrategy.CRYPTIC_DONOR, new CrypticDonorScorer(annotator, generator))
                .put(ScoringStrategy.CRYPTIC_DONOR_IN_CANONICAL_POSITION, new CrypticDonorForVariantsInDonorSite(annotator, generator))
                .put(ScoringStrategy.CANONICAL_ACCEPTOR, new CanonicalAcceptorScorer(annotator, generator))
                .put(ScoringStrategy.CRYPTIC_ACCEPTOR, new CrypticAcceptorScorer(annotator, generator))
                .put(ScoringStrategy.CRYPTIC_ACCEPTOR_IN_CANONICAL_POSITION, new CrypticAcceptorForVariantsInAcceptorSite(annotator, generator))
                .build();
    }


    @Override
    public Function<SplicingTernate, Double> scorerForStrategy(ScoringStrategy strategy) {
        final SplicingScorer scorer = scorerMap.get(strategy);
        if (scorer != null) {
            return scorer.scoringFunction();
        } else {
            LOGGER.warn("Unknown scoring strategy {}", strategy);
            return nullScorer();
        }

    }
}