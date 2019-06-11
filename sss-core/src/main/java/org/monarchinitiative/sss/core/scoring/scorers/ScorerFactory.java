package org.monarchinitiative.sss.core.scoring.scorers;

import org.monarchinitiative.sss.core.pwm.SplicingInformationContentAnnotator;
import org.monarchinitiative.sss.core.reference.allele.AlleleGenerator;


public class ScorerFactory {

    private final SplicingInformationContentAnnotator annotator;

    private final AlleleGenerator alleleGenerator;

    public ScorerFactory(SplicingInformationContentAnnotator annotator) {
        this.annotator = annotator;
        this.alleleGenerator = new AlleleGenerator(annotator.getSplicingParameters());
    }

    public CanonicalDonorScorer getCanonicalDonorScorer() {

        return new CanonicalDonorScorer(annotator, alleleGenerator);
    }

    public CanonicalAcceptorScorer getCanonicalAcceptorScorer() {
        return new CanonicalAcceptorScorer(annotator, alleleGenerator);
    }

    public CrypticDonorScorer getCrypticDonorScorer() {
        return new CrypticDonorScorer(annotator, alleleGenerator);
    }

    public CrypticAcceptorScorer getCrypticAcceptorScorer() {
        return new CrypticAcceptorScorer(annotator, alleleGenerator);
    }

}
