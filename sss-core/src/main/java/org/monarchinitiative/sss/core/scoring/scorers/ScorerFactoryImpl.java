package org.monarchinitiative.sss.core.scoring.scorers;

import org.monarchinitiative.sss.core.pwm.SplicingInformationContentAnnotator;
import org.monarchinitiative.sss.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.sss.core.scoring.ScoringStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ScorerFactoryImpl implements ScorerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScorerFactoryImpl.class);

    private final SplicingInformationContentAnnotator annotator;

    private final AlleleGenerator generator;

    public ScorerFactoryImpl(SplicingInformationContentAnnotator annotator) {
        this.annotator = annotator;
        this.generator = new AlleleGenerator(annotator.getSplicingParameters());
    }


    @Override
    public SplicingScorer scorerForStrategy(ScoringStrategy strategy) {
        switch (strategy) {
            case CANONICAL_DONOR:
                return new CanonicalDonorScorer(annotator, generator);
            case CRYPTIC_DONOR:
                return new CrypticDonorScorer(annotator, generator);
            case CRYPTIC_DONOR_IN_CANONICAL_POSITION:
                return new CrypticDonorForVariantsInDonorSite(annotator, generator);
            case CRYPTIC_ACCEPTOR:
                return new CrypticAcceptorScorer(annotator, generator);
            case CANONICAL_ACCEPTOR:
                return new CanonicalAcceptorScorer(annotator, generator);
            case CRYPTIC_ACCEPTOR_IN_CANONICAL_POSITION:
                return new CrypticAcceptorForVariantsInAcceptorSite(annotator, generator);
            default:
                LOGGER.warn("Unknown scoring strategy '{}'", strategy);
                return (variant, region, sequenceInterval) -> Double.NaN;
        }

    }
}
