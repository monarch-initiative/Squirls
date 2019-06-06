package org.monarchinitiative.sss.ingest.transcripts;

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.monarchinitiative.sss.core.model.SequenceInterval;
import org.monarchinitiative.sss.core.model.SplicingExon;
import org.monarchinitiative.sss.core.model.SplicingIntron;
import org.monarchinitiative.sss.core.model.SplicingTranscript;
import org.monarchinitiative.sss.core.pwm.SplicingInformationContentAnnotator;
import org.monarchinitiative.sss.core.pwm.SplicingParameters;
import org.monarchinitiative.sss.core.reference.GenomeSequenceAccessor;
import org.monarchinitiative.sss.core.reference.InvalidCoordinatesException;
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

    private final SplicingInformationContentAnnotator annotator;


    public SplicingCalculatorImpl(GenomeSequenceAccessor accessor, SplicingInformationContentAnnotator splicingInformationContentAnnotator) {
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
        try {
            si = accessor.fetchSequence(contigName, siBegin, siEnd, txRegion.getStrand().isForward());
        } catch (InvalidCoordinatesException e) {
            LOGGER.warn("Transcript {} has invalid coordinates: {}", model.getAccession(), txRegion, e);
            return Optional.empty();
        }

        SplicingTranscript.Builder builder = SplicingTranscript.newBuilder()
                .setInterval(org.monarchinitiative.sss.core.model.GenomeInterval.newBuilder()
                        .setContig(contigName)
                        .setBegin(txRegion.getBeginPos())
                        .setEnd(txRegion.getEndPos())
                        .setStrand(txRegion.getStrand().isForward())
                        .setContigLength(refDict.getContigIDToLength().get(txRegion.getChr()))
                        .build())
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
            String donorSequence = si.getSequence(intronBegin - parameters.getDonorExonic(), intronBegin + parameters.getDonorIntronic());
            double donorScore = annotator.getSpliceDonorScore(donorSequence);

            int intronEnd = exonRegions.get(i).getBeginPos();
            String acceptorSequence = si.getSequence(intronEnd - parameters.getAcceptorIntronic(), intronEnd + parameters.getAcceptorExonic());
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
