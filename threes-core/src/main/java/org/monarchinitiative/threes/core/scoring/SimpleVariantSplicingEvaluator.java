package org.monarchinitiative.threes.core.scoring;


import de.charite.compbio.jannovar.reference.*;
import org.monarchinitiative.threes.core.data.SplicingTranscriptSource;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessor;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.*;
import java.util.stream.Collectors;

public class SimpleVariantSplicingEvaluator implements VariantSplicingEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleVariantSplicingEvaluator.class);

    /**
     * How much extra sequence to fetch from both sides of a transcript.
     */
    private static final int PADDING = 50;

    private final GenomeSequenceAccessor accessor;

    private final SplicingTranscriptSource transcriptSource;

    private final SplicingAnnotator evaluator;

    public SimpleVariantSplicingEvaluator(GenomeSequenceAccessor accessor,
                                          SplicingTranscriptSource transcriptSource,
                                          SplicingAnnotator evaluator) {
        this.accessor = accessor;
        this.transcriptSource = transcriptSource;
        this.evaluator = evaluator;
    }

    @Override
    public Map<String, SplicingPathogenicityData> evaluate(GenomeVariant variant) {
        final GenomeInterval variantIntervalOnFwd = variant.getGenomeInterval().withStrand(Strand.FWD);

        // 0 - find transcripts we are going to use to evaluate the variant
        final List<SplicingTranscript> transcripts = transcriptSource.fetchTranscripts(variant.getChrName(), variantIntervalOnFwd.getBeginPos(), variantIntervalOnFwd.getEndPos(), variantIntervalOnFwd.getRefDict());
        if (transcripts.isEmpty()) {
            LOGGER.warn("No transcript found for variant `{}`", variant);
            return Collections.emptyMap();
        }

        // 1 - fetch reference sequence from FASTA file
        final GenomeInterval longestTranscriptQueryInterval = transcripts.stream()
                .max(Comparator.comparing(st -> st.getTxRegionCoordinates().length()))
                .get() // guaranteed to contain at least single transcript since we checked the `transcripts` list above
                .getTxRegionCoordinates().withMorePadding(PADDING, PADDING);
        final Optional<SequenceInterval> sequenceInterval = accessor.fetchSequence(longestTranscriptQueryInterval);
        if (sequenceInterval.isEmpty()) {
            LOGGER.warn("Unable to fetch reference sequence for region `{}` to evaluate variant `{}`", longestTranscriptQueryInterval, variant);
            return Collections.emptyMap();
        }

        // 2 - evaluate the variant with respect to all transcripts
        return transcripts.stream()
                .collect(Collectors.toMap(SplicingTranscript::getAccessionId, st -> evaluator.evaluate(variant, st, sequenceInterval.get())));
    }


    @Override
    public Map<String, SplicingPathogenicityData> evaluate(String contig, int pos, String ref, String alt) {
        if (!accessor.getReferenceDictionary().getContigNameToID().containsKey(contig)) {
            LOGGER.warn("Unknown chromosome in query `{}:{}{}>{}`", contig, pos, ref, alt);
            return Collections.emptyMap();
        }
        final int contigId = accessor.getReferenceDictionary().getContigNameToID().get(contig);
        final GenomePosition variantPosition = new GenomePosition(accessor.getReferenceDictionary(), Strand.FWD, contigId, pos, PositionType.ONE_BASED);
        final GenomeVariant variant = new GenomeVariant(variantPosition, ref, alt);
        return evaluate(variant);
    }
}
