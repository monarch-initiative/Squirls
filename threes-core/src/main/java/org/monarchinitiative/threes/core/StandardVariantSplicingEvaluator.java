package org.monarchinitiative.threes.core;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.*;
import org.monarchinitiative.threes.core.classifier.*;
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

    private static final int PADDING = 100;

    private final GenomeSequenceAccessor accessor;

    private final ReferenceDictionary rd;

    private final SplicingTranscriptSource txSource;

    private final SplicingAnnotator annotator;

    private final OverlordClassifier classifier;

    private final Strategy strategy;

    // TODO: 29. 5. 2020 implement in scoring
    private final int maxVariantLength;

    private final ScalingParameters scalingParameters;

    private StandardVariantSplicingEvaluator(Builder builder) {
        accessor = Objects.requireNonNull(builder.accessor, "Accessor cannot be null");
        rd = builder.accessor.getReferenceDictionary();
        txSource = Objects.requireNonNull(builder.txSource, "Transcript source cannot be null");
        annotator = Objects.requireNonNull(builder.annotator, "Annotator cannot be null");
        classifier = Objects.requireNonNull(builder.classifier, "Classifier cannot be null");
        strategy = builder.strategy;

        if (builder.maxVariantLength < 1) {
            String msg = String.format("Maximum variant length cannot be less than 1: %d", builder.maxVariantLength);
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
        maxVariantLength = builder.maxVariantLength;
        scalingParameters = builder.scalingParameters;
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
                final Prediction prediction = classifier.predict(featureData);
                resultMap.put(txId, applyTransformation(prediction));
            } catch (PredictionException e) {
                LOGGER.debug("Error while computing scores for `{}` with respect to `{}`: {}", variant, txId, e.getMessage());
            }
        }
        return resultMap;
    }

    /**
     * Apply transformation corresponding to current {@link #strategy}. Now this means that score is either left as is
     * or it is scaled using logistic regression coefficients.
     *
     * @param prediction to be transformed
     * @return transformed prediction
     */
    private Prediction applyTransformation(Prediction prediction) {
        switch (strategy) {
            case STANDARD:
            default:
                return prediction;
            case SCALING:
                // perform scaling
                // - find the most pathogenic score
                final OptionalDouble maxPathogenicity = prediction.getFragments().stream()
                        .mapToDouble(StandardPrediction.Fragment::getPathoProba)
                        .max();

                // transform the score (benign is default)
                double transformedScore = maxPathogenicity.isPresent() ? transform(maxPathogenicity.getAsDouble()) : 0.;

                // return the transformed score
                return StandardPrediction.builder()
                        .addProbaThresholdPair(transformedScore, scalingParameters.getThreshold())
                        .build();
        }
    }

    /**
     * Logistic regression in small scale. Apply <code>slope</code> and <code>intercept</code>, then scale with
     * sigmoid function.
     *
     * @param x probability value about to be transformed, expecting value in range [0,1]
     * @return transformed probability value clipped to be in range [0,1] if necessary
     */
    double transform(double x) {
        // apply the logistic regression transformation
        final double exp = Math.exp(-(scalingParameters.getSlope() * x + scalingParameters.getIntercept()));
        double score = 1 / (1 + exp);

        // make sure we stay between 0.0 and 1.0
        return Math.max(0., Math.min(1., score));
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


    /**
     * Strategy to use when calculating pathogenicity prediction for a variant.
     */
    public enum Strategy {
        /**
         * Standard strategy selects all pathogenicity scores that are above the threshold and then returns the highest value.
         * If no score is above the threshold, 0.0 is returned.
         */
        STANDARD,
        /**
         * Scaling strategy uses logistic regression to scale the pathogenicity scores.
         */
        SCALING;

        public static Strategy parseStrategy(String strategyString) {
            switch (strategyString.toUpperCase()) {
                case "SCALING":
                    return SCALING;
                case "STANDARD":
                default:
                    return STANDARD;
            }
        }
    }

    public static final class Builder {
        private GenomeSequenceAccessor accessor;
        private SplicingTranscriptSource txSource;
        private SplicingAnnotator annotator;
        private OverlordClassifier classifier;
        // TODO: 29. 5. 2020 change to SCALING as soon as possible
        private Strategy strategy = Strategy.STANDARD;
        private int maxVariantLength = 100;
        private ScalingParameters scalingParameters = ScalingParameters.defaultParameters();

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

        public Builder strategy(Strategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public Builder maxVariantLength(int maxVariantLength) {
            this.maxVariantLength = maxVariantLength;
            return this;
        }

        public Builder scalingParameters(ScalingParameters scalingParameters) {
            this.scalingParameters = scalingParameters;
            return this;
        }

        public StandardVariantSplicingEvaluator build() {
            return new StandardVariantSplicingEvaluator(this);
        }
    }
}
