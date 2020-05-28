package org.monarchinitiative.threes.core;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.*;
import org.monarchinitiative.threes.core.classifier.FeatureData;
import org.monarchinitiative.threes.core.classifier.OverlordClassifier;
import org.monarchinitiative.threes.core.classifier.PredictionException;
import org.monarchinitiative.threes.core.data.SplicingTranscriptSource;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.scoring.SplicingAnnotator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessor;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StandardVariantSplicingEvaluator implements VariantSplicingEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardVariantSplicingEvaluator.class);

    private static final int PADDING = 100;

    private final GenomeSequenceAccessor accessor;

    private final ReferenceDictionary rd;

    private final SplicingTranscriptSource txSource;

    private final SplicingAnnotator annotator;

    private final OverlordClassifier classifier;


    public StandardVariantSplicingEvaluator(GenomeSequenceAccessor genomeSequenceAccessor,
                                            SplicingTranscriptSource splicingTranscriptSource,
                                            SplicingAnnotator splicingAnnotator,
                                            OverlordClassifier overlordClassifier) {
        this.accessor = genomeSequenceAccessor;
        this.rd = genomeSequenceAccessor.getReferenceDictionary();
        this.txSource = splicingTranscriptSource;
        this.annotator = splicingAnnotator;
        this.classifier = overlordClassifier;
    }

    @Override
    public Map<String, Prediction> evaluate(String contig, int pos, String ref, String alt, Set<String> txIds) {
        if (!rd.getContigNameToID().containsKey(contig)) {
            // unknown contig, nothing to be done here
            LOGGER.info("Unknown contig for variant {}:{}{}>{}", contig, pos, ref, alt);
            return Map.of();
        }

        /*
         0 - get overlapping splicing transcripts. Query by coordinates if no txIDs are provided
         */
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, rd.getContigNameToID().get(contig), pos, PositionType.ONE_BASED), ref, alt);
        final GenomeInterval variantInterval = variant.getGenomeInterval();
        final Map<String, SplicingTranscript> txMap = fetchTranscripts(contig, variantInterval.getBeginPos(), variantInterval.getEndPos(), txIds);

        if (txMap.isEmpty()) {
            // no transcript to evaluate
            return Map.of();
        }

        /*
         1 - get enough reference sequence for evaluation with respect to all transcripts
         */
        GenomePosition bp = null, ep = null;
        for (SplicingTranscript tx : txMap.values()) {
            final GenomeInterval txIntervalFwd = tx.getTxRegionCoordinates().withStrand(Strand.FWD);
            if (bp == null || bp.isGt(txIntervalFwd.getGenomeBeginPos())) {
                bp = txIntervalFwd.getGenomeBeginPos();
            }
            if (ep == null || ep.isLt(txIntervalFwd.getGenomeEndPos())) {
                ep = txIntervalFwd.getGenomeEndPos();
            }
        }
        final GenomeInterval toFetch = new GenomeInterval(bp.shifted(-PADDING), ep.differenceTo(bp) + 2 * PADDING);
        final Optional<SequenceInterval> sio = accessor.fetchSequence(toFetch);
        if (sio.isEmpty()) {
            LOGGER.warn("Unable to get reference sequence for `{}`", toFetch);
            return Map.of();
        }
        final SequenceInterval si = sio.get();

        /*
         2 - for each transcript:
             - calculate features
             - calculate probabilities & predictions
             - return results
         */
        final Map<String, Prediction> resultMap = new HashMap<>();
        for (String txId : txMap.keySet()) {
            final SplicingTranscript tx = txMap.get(txId);
            final FeatureData featureData = annotator.evaluate(variant, tx, si);
            try {
                resultMap.put(txId, classifier.predict(featureData));
            } catch (PredictionException e) {
                LOGGER.debug("Error while computing scores for `{}` with respect to `{}`: {}", variant, txId, e.getMessage());
            }
        }
        return resultMap;
    }


    /**
     * Use provided variant coordinates OR transcript accession IDs to fetch {@link SplicingTranscript}s from the database.
     *
     * @param contig chromosome string, e.g. `chrX`, or `X`
     * @param begin  0-based (exclusive, BED-style) begin position of the variant
     * @param end    0-based (inclusive, BED-style) end position of the variant
     * @param txIds  set of transcript accession IDs
     * @return map with transcripts group
     */
    private Map<String, SplicingTranscript> fetchTranscripts(String contig, int begin, int end, Set<String> txIds) {
        Map<String, SplicingTranscript> txMap = new HashMap<>();

        if (txIds.isEmpty()) {
            // querying by coordinates
            return txSource.fetchTranscripts(contig, begin, end, accessor.getReferenceDictionary()).stream()
                    .collect(Collectors.toMap(SplicingTranscript::getAccessionId, Function.identity()));

        } else {
            // or query by transcript IDs
            for (String txId : txIds) {
                final Optional<SplicingTranscript> sto = txSource.fetchTranscriptByAccession(txId, accessor.getReferenceDictionary());
                if (sto.isPresent()) {
                    txMap.put(txId, sto.get());
                } else {
                    LOGGER.info("Unknown transcript id `{}`", txId);
                }
            }
        }
        return txMap;
    }
}
