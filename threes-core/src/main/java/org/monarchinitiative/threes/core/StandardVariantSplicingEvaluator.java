package org.monarchinitiative.threes.core;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.*;
import org.monarchinitiative.threes.core.classifier.FeatureData;
import org.monarchinitiative.threes.core.classifier.OverlordClassifier;
import org.monarchinitiative.threes.core.classifier.Prediction;
import org.monarchinitiative.threes.core.classifier.PredictionException;
import org.monarchinitiative.threes.core.classifier.transform.prediction.PredictionTransformer;
import org.monarchinitiative.threes.core.data.SplicingTranscriptSource;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.scoring.SplicingAnnotator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessor;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StandardVariantSplicingEvaluator implements VariantSplicingEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardVariantSplicingEvaluator.class);

    private static final int PADDING = 150;

    private final GenomeSequenceAccessor accessor;

    private final ReferenceDictionary rd;

    private final SplicingTranscriptSource txSource;

    private final SplicingAnnotator annotator;

    private final OverlordClassifier classifier;

    private final PredictionTransformer transformer;

    private final int maxVariantLength;

    /**
     * Amount of neighboring FASTA sequence fetched for each variant.
     */
    private final int padding;


    private StandardVariantSplicingEvaluator(Builder builder) {
        accessor = Objects.requireNonNull(builder.accessor, "Accessor cannot be null");
        rd = builder.accessor.getReferenceDictionary();
        txSource = Objects.requireNonNull(builder.txSource, "Transcript source cannot be null");
        annotator = Objects.requireNonNull(builder.annotator, "Annotator cannot be null");
        classifier = Objects.requireNonNull(builder.classifier, "Classifier cannot be null");
        transformer = Objects.requireNonNull(builder.transformer, "Prediction transformer cannot be null");

        if (builder.maxVariantLength < 1) {
            String msg = String.format("Maximum variant length cannot be less than 1: %d", builder.maxVariantLength);
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
        maxVariantLength = builder.maxVariantLength;
        padding = PADDING + maxVariantLength;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Map<String, Prediction> evaluate(String contig, int pos, String ref, String alt, Set<String> txIds) {
        if (!rd.getContigNameToID().containsKey(contig)) {
            // unknown contig, nothing to be done here
            LOGGER.info("Unknown contig for variant {}:{}{}>{}", contig, pos, ref, alt);
            return Map.of();
        }

        // do not process variants that are longer than preset value
        if (ref.length() > maxVariantLength) {
            LOGGER.debug("Not evaluating variant longer than maximum variant length: `{}` > `{}` for `{}:{}{}>{}`",
                    ref.length(), maxVariantLength, contig, pos, ref, alt);
            return Map.of();
        }

        /*
         0 - get overlapping splicing transcripts. Query by coordinates if no txIDs are provided. Only transcripts
         that overlap with the variant interval are considered.
         */
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, rd.getContigNameToID().get(contig), pos, PositionType.ONE_BASED), ref, alt);
        final Map<String, SplicingTranscript> txMap = fetchTranscripts(variant, txIds);

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

        // PADDING + maxVariantLength should provide enough sequence in most cases
        final GenomeInterval toFetch = new GenomeInterval(bp.shifted(-padding), ep.differenceTo(bp) + 2 * padding);
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
                final Prediction prediction = classifier.predict(featureData);
                final Prediction transformed = transformer.transform(prediction);
                resultMap.put(txId, transformed);
            } catch (PredictionException e) {
                LOGGER.debug("Error while computing scores for `{}` with respect to `{}`: {}", variant, txId, e.getMessage());
            }
        }
        return resultMap;
    }

    /**
     * Use provided variant coordinates <em>OR</em> transcript accession IDs to fetch {@link SplicingTranscript}s from
     * the database.
     * <p>
     * Only transcripts that overlap with the <code>variant</code> are returned.
     * </p>
     *
     * @param variant {@link GenomeVariant} with variant coordinates
     * @param txIds   set of transcript accession IDs
     * @return map with transcripts group
     */
    private Map<String, SplicingTranscript> fetchTranscripts(GenomeVariant variant, Set<String> txIds) {
        final Map<String, SplicingTranscript> txMap = new HashMap<>();
        final GenomeInterval variantInterval = variant.getGenomeInterval();

        if (txIds.isEmpty()) {
            // querying by coordinates
            return txSource.fetchTranscripts(variant.getChrName(), variantInterval.getBeginPos(), variantInterval.getEndPos(), accessor.getReferenceDictionary()).stream()
                    .collect(Collectors.toMap(SplicingTranscript::getAccessionId, Function.identity()));

        } else {
            // or query by transcript IDs
            for (String txId : txIds) {
                final Optional<SplicingTranscript> sto = txSource.fetchTranscriptByAccession(txId, accessor.getReferenceDictionary());
                if (sto.isPresent()) {
                    final SplicingTranscript st = sto.get();
                    // transcript must overlap with the variant
                    if (st.getTxRegionCoordinates().overlapsWith(variantInterval)) {
                        txMap.put(txId, st);
                    }
                } else {
                    LOGGER.debug("Unknown transcript id `{}`", txId);
                }
            }
        }
        return txMap;
    }

    public static final class Builder {
        private GenomeSequenceAccessor accessor;
        private SplicingTranscriptSource txSource;
        private SplicingAnnotator annotator;
        private OverlordClassifier classifier;
        private PredictionTransformer transformer;

        private int maxVariantLength = 100;

        private Builder() {
        }

        public Builder accessor(GenomeSequenceAccessor accessor) {
            this.accessor = accessor;
            return this;
        }

        public Builder txSource(SplicingTranscriptSource txSource) {
            this.txSource = txSource;
            return this;
        }

        public Builder annotator(SplicingAnnotator annotator) {
            this.annotator = annotator;
            return this;
        }

        public Builder classifier(OverlordClassifier classifier) {
            this.classifier = classifier;
            return this;
        }

        public Builder transformer(PredictionTransformer transformer) {
            this.transformer = transformer;
            return this;
        }

        public Builder maxVariantLength(int maxVariantLength) {
            this.maxVariantLength = maxVariantLength;
            return this;
        }

        public StandardVariantSplicingEvaluator build() {
            return new StandardVariantSplicingEvaluator(this);
        }
    }
}
