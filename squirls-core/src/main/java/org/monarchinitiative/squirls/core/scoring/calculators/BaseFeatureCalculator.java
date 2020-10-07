package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.reference.SplicingLocationData;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.squirls.core.scoring.Annotatable;
import org.monarchinitiative.squirls.core.scoring.SequenceRegion;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;

abstract class BaseFeatureCalculator implements FeatureCalculator {

    private static final String FASTA_TRACK_NAME = "fasta";

    protected final SplicingInformationContentCalculator calculator;

    protected final AlleleGenerator generator;

    private final SplicingTranscriptLocator locator;

    protected BaseFeatureCalculator(SplicingInformationContentCalculator calculator,
                                    AlleleGenerator generator, SplicingTranscriptLocator locator) {
        this.calculator = calculator;
        this.generator = generator;
        this.locator = locator;
    }

    @Override
    public <T extends Annotatable> double score(T data) {
        final SplicingLocationData locationData = locator.locate(data.getVariant(), data.getTranscript());
        return (data.getTrackNames().contains(FASTA_TRACK_NAME))
                ? score(data.getVariant(), locationData, data.getTrack(FASTA_TRACK_NAME, SequenceRegion.class))
                : Double.NaN;
    }

    protected abstract double score(GenomeVariant variant, SplicingLocationData locationData, SequenceRegion sequence);
}
