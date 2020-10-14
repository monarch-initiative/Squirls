package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.reference.SplicingLocationData;
import org.monarchinitiative.squirls.core.reference.transcript.SplicingTranscriptLocator;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

/**
 * Calculate length of the exon the variant is located in. The length is calculated only for variants with
 * {@link org.monarchinitiative.squirls.core.reference.SplicingLocationData.SplicingPosition#DONOR},
 * {@link org.monarchinitiative.squirls.core.reference.SplicingLocationData.SplicingPosition#ACCEPTOR}, and
 * {@link org.monarchinitiative.squirls.core.reference.SplicingLocationData.SplicingPosition#EXON}.
 * <p>
 * For the remaining variants, <code>-1</code> is returned.
 */
public class ExonLength implements FeatureCalculator {

    private final SplicingTranscriptLocator locator;

    public ExonLength(SplicingTranscriptLocator locator) {
        this.locator = locator;
    }

    @Override
    public double score(GenomeVariant variant, SplicingTranscript transcript, SequenceInterval sequence) {
        final SplicingLocationData locationData = locator.locate(variant, transcript);
        final SplicingLocationData.SplicingPosition position = locationData.getPosition();
        switch (position) {
            case DONOR:
            case ACCEPTOR:
            case EXON:
                return transcript.getExons().get(locationData.getExonIdx()).getInterval().length();
            case OUTSIDE:
            case INTRON:
            default:
                return -1;
        }
    }
}
