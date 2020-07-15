package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.reference.SplicingLocationData;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

abstract class BaseFeatureCalculator implements FeatureCalculator {

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
    public double score(GenomeVariant variant, SplicingTranscript transcript, SequenceInterval sequence) {
        final SplicingLocationData data = locator.locate(variant, transcript);
        return score(variant, data, sequence);
    }

    protected abstract double score(GenomeVariant variant, SplicingLocationData locationData, SequenceInterval sequence);
}
