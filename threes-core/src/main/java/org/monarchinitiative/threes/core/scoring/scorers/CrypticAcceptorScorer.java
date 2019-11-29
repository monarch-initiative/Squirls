package org.monarchinitiative.threes.core.scoring.scorers;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.threes.core.Utils;
import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.model.SplicingIntron;
import org.monarchinitiative.threes.core.model.SplicingRegion;
import org.monarchinitiative.threes.core.model.SplicingTernate;
import org.monarchinitiative.threes.core.reference.allele.AlleleGenerator;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.Optional;
import java.util.function.Function;

/**
 *
 */
public class CrypticAcceptorScorer implements SplicingScorer {

    private static final int MAX_IN_INTRON = 50;

    private final int maxInIntron;

    private final SplicingInformationContentCalculator icAnnotator;

    private final AlleleGenerator generator;

    private final int acceptorLength;

    public CrypticAcceptorScorer(SplicingInformationContentCalculator icAnnotator, AlleleGenerator generator) {
        this(icAnnotator, generator, MAX_IN_INTRON);
    }

    public CrypticAcceptorScorer(SplicingInformationContentCalculator icAnnotator, AlleleGenerator generator, int maxInIntron) {
        this.icAnnotator = icAnnotator;
        this.generator = generator;
        this.acceptorLength = icAnnotator.getSplicingParameters().getAcceptorLength();
        this.maxInIntron = maxInIntron;
    }

    @Override
    public Function<SplicingTernate, Double> scoringFunction() {
        return t -> {
            final SplicingRegion region = t.getRegion();
            final SequenceInterval sequenceInterval = t.getSequenceInterval();
            final GenomeVariant variant = t.getVariant();

            if (!(region instanceof SplicingIntron)) {
                return Double.NaN;
            }
            final SplicingIntron intron = (SplicingIntron) region;
            // this scorer is applied when variant does not overlap with canonical acceptor site. Here we ensure that it really
            // does not overlap the acceptor site
            final GenomeInterval acceptor = generator.makeAcceptorRegion(intron);
            final GenomeInterval variantInterval = variant.getGenomeInterval();
            if (acceptor.overlapsWith(variantInterval)) {
                return Double.NaN;
            }
            final int diff = intron.getInterval().getGenomeEndPos().differenceTo(variantInterval);
            if (diff > maxInIntron) {
                // we do not score intronic variant that are too deep in intron
                return Double.NaN;
            }

            final GenomeInterval upstream = new GenomeInterval(variantInterval.getGenomeBeginPos().shifted(-acceptorLength + 1), acceptorLength - 1);
            final GenomeInterval downstream = new GenomeInterval(variantInterval.getGenomeEndPos(), acceptorLength - 1);

            final Optional<String> upsSeq = sequenceInterval.getSubsequence(upstream);
            final Optional<String> downSeq = sequenceInterval.getSubsequence(downstream);
            String altSnippet = upsSeq.get() + variant.getAlt() + downSeq.get();

            double wtCanonicalAcceptorScore = intron.getAcceptorScore();

            double altCrypticAcceptorScore = Utils.slidingWindow(altSnippet, acceptorLength)
                    .map(icAnnotator::getSpliceAcceptorScore)
                    .max(Double::compareTo)
                    .orElse(Double.NaN);
            return altCrypticAcceptorScore - wtCanonicalAcceptorScore;
        };
    }
}
