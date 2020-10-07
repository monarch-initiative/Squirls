package org.monarchinitiative.squirls.core;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.*;
import org.monarchinitiative.squirls.core.classifier.SquirlsClassifier;
import org.monarchinitiative.squirls.core.classifier.transform.prediction.PredictionTransformer;
import org.monarchinitiative.squirls.core.data.SplicingAnnotationData;
import org.monarchinitiative.squirls.core.data.SplicingAnnotationDataSource;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.scoring.SplicingAnnotator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StandardVariantSplicingEvaluator implements VariantSplicingEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardVariantSplicingEvaluator.class);

    /**
     * This reference dictionary represents data that is within SQUIRLS database.
     */
    private final ReferenceDictionary rd;

    private final SplicingAnnotationDataSource annSource;

    private final SplicingAnnotator annotator;

    private final SquirlsClassifier classifier;

    private final PredictionTransformer transformer;

    private final int maxVariantLength;

    private StandardVariantSplicingEvaluator(Builder builder) {
        annSource = Objects.requireNonNull(builder.annSource, "Annotation source cannot be null");
        rd = annSource.getReferenceDictionary();

        annotator = Objects.requireNonNull(builder.annotator, "Annotator cannot be null");
        classifier = Objects.requireNonNull(builder.classifier, "Classifier cannot be null");
        transformer = Objects.requireNonNull(builder.transformer, "Prediction transformer cannot be null");

        if (builder.maxVariantLength < 1) {
            String msg = String.format("Maximum variant length cannot be less than 1: %d", builder.maxVariantLength);
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
        maxVariantLength = builder.maxVariantLength;
    }

    public static Builder builder() {
        return new Builder();
    }

    private static Predicate<? super SplicingTranscript> transcriptsWithNoIntrons() {
        return tm -> !tm.getIntrons().isEmpty();
    }

    /**
     * Evaluate given variant with respect to transcripts in <code>txIds</code>. The method <em>attempts</em> to evaluate
     * the variant with respect to given <code>txIds</code>, but does not guarantee that results will be provided for
     * each transcript.
     * <p>
     * Note that only transcripts with at least 2 exons are considered.
     *
     * @param contig string with name of the chromosome
     * @param pos    1-based (included) variant position on FWD strand (VCF-style)
     * @param ref    reference allele, e.g. `C`, `CCT`
     * @param alt    alternate allele, e.g. `T`, `AA`
     * @param txIds  set of transcript accession IDs with respect to which the variant should be evaluated
     * @return possibly empty map with {@link SplicingPredictionData} for transcript ID
     */
    @Override
    public Map<String, SplicingPredictionData> evaluate(String contig, int pos, String ref, String alt, Set<String> txIds) {
        // perform some sanity checks at the beginning
        if (variantFailsInputCheck(contig, pos, ref, alt)) {
            // check failed
            return Map.of();
        }
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, rd.getContigNameToID().get(contig), pos, PositionType.ONE_BASED), ref, alt);
        final GenomeInterval varInterval = variant.getGenomeInterval();
        final Map<String, SplicingAnnotationData> annotationData = annSource.getAnnotationData(contig, varInterval.getBeginPos(), varInterval.getEndPos())
                // let's filter the results to remove transcripts we're not interested in on the fly
                .entrySet().stream()
                .filter(e -> e.getValue().getTranscripts().stream().anyMatch(st -> txIds.contains(st.getAccessionId())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


        return evaluateVariantAgainstTranscripts(variant, annotationData);
    }

    @Override
    public Map<String, SplicingPredictionData> evaluate(String contig, int pos, String ref, String alt) {
        // perform some sanity checks at the beginning
        if (variantFailsInputCheck(contig, pos, ref, alt)) {
            // check failed
            return Map.of();
        }
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, rd.getContigNameToID().get(contig), pos, PositionType.ONE_BASED), ref, alt);
        final GenomeInterval varInterval = variant.getGenomeInterval();
        final Map<String, SplicingAnnotationData> annotationData = annSource.getAnnotationData(contig, varInterval.getBeginPos(), varInterval.getEndPos());

        return evaluateVariantAgainstTranscripts(variant, annotationData);
    }

    private Map<String, SplicingPredictionData> evaluateVariantAgainstTranscripts(GenomeVariant variant, Map<String, SplicingAnnotationData> annotationData) {
        final Map<String, SplicingPredictionData> predictions = new ConcurrentHashMap<>();
        for (String geneSymbol : annotationData.keySet()) {
            final SplicingAnnotationData data = annotationData.get(geneSymbol);
            data.getTranscripts().parallelStream()
                    .filter(transcriptsWithNoIntrons())
                    .map(tx -> StandardSplicingPredictionData.of(variant, tx, data.getTracks()))
                    .map(annotator::annotate)
                    .map(classifier::predict)
                    .map(transformer::transform)
                    .forEach(k -> predictions.put(k.getTranscript().getAccessionId(), SimpleSplicingPredictionData.copyOf(k)));
        }
        return predictions;
    }

    private boolean variantFailsInputCheck(String contig, int pos, String ref, String alt) {
        if (!rd.getContigNameToID().containsKey(contig)) {
            // unknown contig, nothing to be done here
            LOGGER.info("Unknown contig for variant {}:{}{}>{}", contig, pos, ref, alt);
            return true;
        }

        // do not process variants that are longer than preset value
        if (ref.length() > maxVariantLength) {
            LOGGER.debug("Not evaluating variant longer than maximum variant length: `{}` > `{}` for `{}:{}{}>{}`",
                    ref.length(), maxVariantLength, contig, pos, ref, alt);
            return true;
        }
        return false;
    }

    public static final class Builder {

        private SplicingAnnotationDataSource annSource;
        private SplicingAnnotator annotator;
        private SquirlsClassifier classifier;
        private PredictionTransformer transformer;

        private int maxVariantLength = 100;

        private Builder() {
        }

        public Builder annDataSource(SplicingAnnotationDataSource annSource) {
            this.annSource = annSource;
            return this;
        }

        public Builder annotator(SplicingAnnotator annotator) {
            this.annotator = annotator;
            return this;
        }

        public Builder classifier(SquirlsClassifier classifier) {
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
