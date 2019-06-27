package org.monarchinitiative.threes.ingest.transcripts;

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.model.*;
import org.monarchinitiative.threes.core.reference.fasta.GenomeSequenceAccessor;
import org.monarchinitiative.threes.core.reference.fasta.InvalidCoordinatesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        ReferenceDictionary refDict = model.getTXRegion().getRefDict();
        GenomeInterval txRegion = model.getTXRegion();

        String contigName = refDict.getContigIDToName().get(txRegion.getChr());
        int siBegin = txRegion.getBeginPos() - SEQ_INT_PADDING;
        int siEnd = txRegion.getEndPos() + SEQ_INT_PADDING;
        SequenceInterval si;
        final GenomeCoordinates coordinates;
        try {
            si = accessor.fetchSequence(contigName, siBegin, siEnd, txRegion.getStrand().isForward());
            coordinates = GenomeCoordinates.newBuilder()
                    .setContig(contigName)
                    .setBegin(txRegion.getBeginPos())
                    .setEnd(txRegion.getEndPos())
                    .setStrand(txRegion.getStrand().isForward())
                    .build();
        } catch (InvalidCoordinatesException e) {
            LOGGER.warn("Transcript {} has invalid coordinates: {}", model.getAccession(), txRegion, e);
            return Optional.empty();
        }


        SplicingTranscript.Builder builder = SplicingTranscript.newBuilder()
                .setCoordinates(coordinates)
                .setAccessionId(model.getAccession());


        ImmutableList<GenomeInterval> exonRegions = model.getExonRegions();
        if (exonRegions.isEmpty()) {
            // no exons
            return Optional.empty();
        }


        // add first exon which may also be last if transcript consists of only single exon
        GenomeInterval firstExon = exonRegions.get(0);
        builder.addExon(
                SplicingExon.newBuilder()
                        .setBegin(firstExon.getBeginPos())
                        .setEnd(firstExon.getEndPos())
                        .build());

        SplicingParameters parameters = annotator.getSplicingParameters();
        for (int i = 1; i < exonRegions.size(); i++) {
            // we have more than one exon, therefore we also have at least single intron
            // we start at i = 1, since we already processed the first exon above
            int intronBegin = exonRegions.get(i - 1).getEndPos();
            String donorSequence = si.getSubsequence(intronBegin - parameters.getDonorExonic(), intronBegin + parameters.getDonorIntronic());
            double donorScore = annotator.getSpliceDonorScore(donorSequence);

            int intronEnd = exonRegions.get(i).getBeginPos();
            String acceptorSequence = si.getSubsequence(intronEnd - parameters.getAcceptorIntronic(), intronEnd + parameters.getAcceptorExonic());
            double acceptorScore = annotator.getSpliceAcceptorScore(acceptorSequence);

            int exonEnd = exonRegions.get(i).getEndPos();

            builder.addIntron(SplicingIntron.newBuilder()
                    .setBegin(intronBegin)
                    .setEnd(intronEnd)
                    .setDonorScore(donorScore)
                    .setAcceptorScore(acceptorScore)
                    .build())
                    .addExon(SplicingExon.newBuilder()
                            .setBegin(intronEnd)
                            .setEnd(exonEnd)
                            .build());
        }

        return Optional.of(builder.build());
    }


}
