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
 * This calculator computes the feature <code>sstrength_diff_donor</code> denoting the difference between the donor
 * Ri of the current exon and the downstream exon of the transcript.
 * <p>
 * The calculator only works for coding variants and variants overlapping with the canonical donor sites of all exons
 * except for the second last and the last exon.
 */
public class SStrengthDiffDonor implements FeatureCalculator {

    private final SplicingInformationContentCalculator calculator;
    private final AlleleGenerator generator;
    private final SplicingTranscriptLocator locator;

    public SStrengthDiffDonor(SplicingInformationContentCalculator calculator,
                              AlleleGenerator generator,
                              SplicingTranscriptLocator locator) {
        this.calculator = calculator;
        this.generator = generator;
        this.locator = locator;
    }

    @Override
    public double score(GenomeVariant variant, SplicingTranscript transcript, SequenceInterval sequence) {
        final SplicingLocationData locationData = locator.locate(variant, transcript);
        switch (locationData.getPosition()) {
            case EXON:
            case DONOR:
                final int exonIdx = locationData.getExonIdx();
                if (transcript.getExons().size() - exonIdx > 2) {
                    // the current exon is NOT the last or the second last exon of the transcript
                    final GenomePosition thisDonorAnchor = transcript.getExons().get(exonIdx).getInterval().getGenomeEndPos();
                    final String thisDonorSiteSnippet = generator.getDonorSiteWithAltAllele(thisDonorAnchor, variant, sequence);
                    final GenomePosition nextDonorAnchor = transcript.getExons().get(exonIdx + 1).getInterval().getGenomeEndPos();
                    final String nextDonorSiteSnippet = generator.getDonorSiteWithAltAllele(nextDonorAnchor, variant, sequence);

                    if (thisDonorSiteSnippet != null && nextDonorSiteSnippet != null) {
                        return calculator.getSpliceDonorScore(thisDonorSiteSnippet) - calculator.getSpliceDonorScore(nextDonorSiteSnippet);
                    }
                }
            case INTRON:
            case ACCEPTOR:
            case OUTSIDE:
            default:
                return 0.;
        }
    }
}
