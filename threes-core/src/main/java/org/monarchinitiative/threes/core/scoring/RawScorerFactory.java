package org.monarchinitiative.threes.core.scoring;

import com.google.common.collect.ImmutableMap;
import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.calculators.sms.SMSCalculator;
import org.monarchinitiative.threes.core.model.SplicingTernate;
import org.monarchinitiative.threes.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.threes.core.scoring.scorers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;


/**
 *
 */
public class RawScorerFactory implements ScorerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawScorerFactory.class);

    private final ImmutableMap<ScoringStrategy, SplicingScorer> scorerMap;

    public RawScorerFactory(SplicingInformationContentCalculator annotator, SMSCalculator calculator,
                            int maxDistanceExonDownstream, int maxDistanceExonUpstream) {
        AlleleGenerator generator = new AlleleGenerator(annotator.getSplicingParameters());
        this.scorerMap = ImmutableMap.<ScoringStrategy, SplicingScorer>builder()
                .put(ScoringStrategy.CANONICAL_DONOR, new CanonicalDonorScorer(annotator, generator))
                .put(ScoringStrategy.CRYPTIC_DONOR, new CrypticDonorScorer(annotator, generator, maxDistanceExonDownstream))
                .put(ScoringStrategy.CRYPTIC_DONOR_IN_CANONICAL_POSITION, new CrypticDonorForVariantsInDonorSite(annotator, generator))
                .put(ScoringStrategy.CANONICAL_ACCEPTOR, new CanonicalAcceptorScorer(annotator, generator))
                .put(ScoringStrategy.CRYPTIC_ACCEPTOR, new CrypticAcceptorScorer(annotator, generator, maxDistanceExonUpstream))
                .put(ScoringStrategy.CRYPTIC_ACCEPTOR_IN_CANONICAL_POSITION, new CrypticAcceptorForVariantsInAcceptorSite(annotator, generator))
                .put(ScoringStrategy.SMS, new SMSScorer(calculator))
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
