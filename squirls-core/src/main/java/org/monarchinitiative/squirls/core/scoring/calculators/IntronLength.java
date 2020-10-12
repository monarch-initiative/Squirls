package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.reference.SplicingLocationData;
import org.monarchinitiative.squirls.core.reference.transcript.SplicingTranscriptLocator;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

public class IntronLength implements FeatureCalculator {

    private final SplicingTranscriptLocator locator;

    public IntronLength(SplicingTranscriptLocator locator) {
        this.locator = locator;
    }

    @Override
    public double score(GenomeVariant variant, SplicingTranscript transcript, SequenceInterval sequence) {
        final SplicingLocationData locationData = locator.locate(variant, transcript);

        final SplicingLocationData.SplicingPosition position = locationData.getPosition();
        switch (position) {
            case INTRON:
                // return length of the current intron
                return transcript.getIntrons().get(locationData.getIntronIdx()).getInterval().length();
            case DONOR:
            case ACCEPTOR:
            case EXON:
                final int exonIdx = locationData.getExonIdx();
                if (transcript.getIntrons().size() - exonIdx > 0) {
                    // the variant is not located in the last exon, let's return length of the downstream intron
                    return transcript.getIntrons().get(exonIdx).getInterval().length();
                }
            case OUTSIDE:
                // or also variants coding in the last exon
            default:
                return -1;
        }
    }
}
