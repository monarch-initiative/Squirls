package org.monarchinitiative.squirls.ingest.transcripts;

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.monarchinitiative.squirls.core.model.SplicingExon;
import org.monarchinitiative.squirls.core.model.SplicingIntron;
import org.monarchinitiative.squirls.core.model.SplicingParameters;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessor;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.Optional;

/**
 *
 */
public class SplicingCalculatorImpl implements SplicingCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SplicingCalculatorImpl.class);

    /**
     * For each {@link TranscriptModel} we fetch +- 100bp sequence
     */
    private static final int SEQ_INT_PADDING = 100;

    private final GenomeSequenceAccessor accessor;

    private final SplicingInformationContentCalculator annotator;


    public SplicingCalculatorImpl(GenomeSequenceAccessor accessor, SplicingInformationContentCalculator splicingInformationContentAnnotator) {
        this.accessor = accessor;
        this.annotator = splicingInformationContentAnnotator;
    }

    @Override
    public Optional<SplicingTranscript> calculate(TranscriptModel model) {
        GenomeInterval txRegion = model.getTXRegion();

        final GenomeInterval query = txRegion.withMorePadding(SEQ_INT_PADDING, SEQ_INT_PADDING);
        Optional<SequenceInterval> siOpt = accessor.fetchSequence(query);
        if (siOpt.isEmpty()) {
            LOGGER.warn("Could not get fasta sequence for transcript `{}`, query region: (+-padding `{}`)", model.getAccession(), query);
            return Optional.empty();
        }
        final SequenceInterval si = siOpt.get();

        SplicingTranscript.Builder builder = SplicingTranscript.builder()
                .setCoordinates(txRegion)
                .setAccessionId(model.getAccession());

        ImmutableList<GenomeInterval> exonRegions = model.getExonRegions();
        if (exonRegions.isEmpty()) {
            // no exons
            return Optional.empty();
        }

        // add first exon which may also be last if transcript consists of only single exon
        GenomeInterval firstExon = exonRegions.get(0);
        builder.addExon(
                SplicingExon.builder()
                        .setInterval(firstExon)
                        .build());

        SplicingParameters parameters = annotator.getSplicingParameters();
        for (int i = 1; i < exonRegions.size(); i++) {
            // we have more than one exon, therefore we also have at least single intron
            // we start at i = 1, since we already processed the first exon above
            final GenomeInterval exonInterval = exonRegions.get(i);

            GenomePosition intronBegin = exonRegions.get(i - 1).getGenomeEndPos();
            GenomePosition intronEnd = exonInterval.getGenomeBeginPos();
            GenomeInterval intronInterval = new GenomeInterval(intronBegin, intronEnd.differenceTo(intronBegin));

            final GenomeInterval donorRegion = new GenomeInterval(intronBegin.shifted(-parameters.getDonorExonic()), parameters.getDonorLength());
            Optional<String> donorSequenceOpt = si.getSubsequence(donorRegion);
            final double donorScore = donorSequenceOpt.map(annotator::getSpliceDonorScore).orElse(Double.NaN);

            final GenomeInterval acceptorRegion = new GenomeInterval(intronEnd.shifted(-parameters.getAcceptorIntronic()), parameters.getAcceptorLength());
            Optional<String> acceptorSequenceOpt = si.getSubsequence(acceptorRegion);
            double acceptorScore = acceptorSequenceOpt.map(annotator::getSpliceAcceptorScore).orElse(Double.NaN);

            builder.addIntron(SplicingIntron.builder()
                    .setInterval(intronInterval)
                    .setDonorScore(donorScore)
                    .setAcceptorScore(acceptorScore)
                    .build())
                    .addExon(SplicingExon.builder()
                            .setInterval(exonInterval)
                            .build());
        }

        try {
            return Optional.of(builder.check(true).build());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Error processing transcript {}: {}", model.getAccession(), e.getMessage());
            return Optional.empty();
        }
    }

}
