package org.monarchinitiative.squirls.core.scoring;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.Metadata;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.reference.SplicingLocationData;
import org.monarchinitiative.squirls.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.squirls.core.scoring.calculators.FeatureCalculator;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.Map;


abstract class AbstractSplicingAnnotator implements SplicingAnnotator {

    private final SplicingTranscriptLocator locator;
    private final Map<String, FeatureCalculator> calculatorMap;

    protected AbstractSplicingAnnotator(SplicingTranscriptLocator locator, Map<String, FeatureCalculator> calculatorMap) {
        this.locator = locator;
        this.calculatorMap = calculatorMap;
    }

    @Override
    public <T extends Annotatable> T annotate(T data) {
        final GenomeVariant variant = data.getVariant();
        final SplicingTranscript transcript = data.getTranscript();
        final SequenceRegion seq = data.getTrack("sequence", SequenceRegion.class);
        final SequenceInterval sequence = SequenceInterval.builder()
                .interval(seq.getInterval())
                .sequence(seq.getValue())
                .build();
        final FloatRegion phylop = data.getTrack("phylop", FloatRegion.class);

        final GenomeVariant variantOnStrand = variant.withStrand(transcript.getStrand());

        // calculate the features
        calculatorMap.forEach((name, calculator) -> data.putFeature(name, calculator.score(variantOnStrand, transcript, sequence, phylop)));

        final SplicingLocationData locationData = locator.locate(variant, transcript);

        final Metadata.Builder metadataBuilder = Metadata.builder();
        locationData.getDonorBoundary().ifPresent(boundary -> metadataBuilder.putDonorCoordinate(transcript.getAccessionId(), boundary));
        locationData.getAcceptorBoundary().ifPresent(boundary -> metadataBuilder.putAcceptorCoordinate(transcript.getAccessionId(), boundary));

        data.setMetadata(metadataBuilder.build());

        return data;
    }
}
