package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.reference.SplicingLocationData;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

/**
 * This calculator computes the feature <code>sstrength_diff_acceptor</code> denoting difference between the acceptor
 * Ri of the current exon and the downstream exon of the transcript.
 * <p>
 * The calculator only works for coding variants and variants overlapping with the canonical donor sites of all exons
 * except for the second last and the last exon.
 */
public class SStrengthDiffAcceptor implements FeatureCalculator {

    private final SplicingInformationContentCalculator calculator;
    private final AlleleGenerator generator;
    private final SplicingTranscriptLocator locator;

    public SStrengthDiffAcceptor(SplicingInformationContentCalculator calculator, AlleleGenerator generator, SplicingTranscriptLocator locator) {
        this.calculator = calculator;
        this.generator = generator;
        this.locator = locator;
    }

    @Override
    public double score(GenomeVariant variant, SplicingTranscript transcript, SequenceInterval sequence) {
        final SplicingLocationData locationData = locator.locate(variant, transcript);
        switch (locationData.getPosition()) {
            case EXON:
            case ACCEPTOR:
                final int exonIdx = locationData.getExonIdx();
                if (transcript.getExons().size() - exonIdx > 1) {
                    // the current exon is NOT the last exon of the transcript
                    final GenomePosition thisAcceptorAnchor = transcript.getExons().get(exonIdx).getInterval().getGenomeBeginPos();
                    final String thisAcceptorSiteSnippet = generator.getAcceptorSiteWithAltAllele(thisAcceptorAnchor, variant, sequence);
                    final GenomePosition nextAcceptorAnchor = transcript.getExons().get(exonIdx + 1).getInterval().getGenomeBeginPos();
                    final String nextAcceptorSiteSnippet = generator.getAcceptorSiteWithAltAllele(nextAcceptorAnchor, variant, sequence);

                    if (thisAcceptorSiteSnippet != null && nextAcceptorSiteSnippet != null) {
                        return calculator.getSpliceAcceptorScore(thisAcceptorSiteSnippet) - calculator.getSpliceAcceptorScore(nextAcceptorSiteSnippet);
                    }
                }
            case INTRON:
            case DONOR:
            case OUTSIDE:
            default:
                return 0.;
        }
    }
}
