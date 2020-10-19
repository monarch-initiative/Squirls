package org.monarchinitiative.squirls.cli.cmd.analyze_vcf;

import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.monarchinitiative.squirls.core.SplicingPredictionData;
import org.monarchinitiative.squirls.core.classifier.Prediction;

import java.util.*;

/**
 * This class is a POJO for a single ALT allele of the variant and all the associated data.
 * <p>
 * <b>BEWARE</b> - This class contains variant data that uses 2 separate and possibly different
 * {@link de.charite.compbio.jannovar.data.ReferenceDictionary} objects!
 * <p>
 * The first dictionary comes from Jannovar and is within
 * {@link VariantAnnotations} object.
 * <p>
 * The second dictionary comes from SQUIRLS database and is used by all objects present within
 * {@link SplicingPredictionData}.
 */
// TODO: 16. 10. 2020 simplify - get rid of the variant context, allele, etc, when work on SplicingVariantGraphicsGenerator is finished
public class SplicingVariantAlleleEvaluation implements VisualizedVariant {

    /**
     * The base variant context that is being analyzed.
     */
    private final VariantContext base;

    /**
     * The ALT allele of the variant context that is being analyzed.
     */
    private final Allele altAllele;

    /**
     * Results of the splicing analysis - map of {@link SplicingPredictionData} with respect to transcript ID.
     */
    private final Map<String, SplicingPredictionData> predictionData = new HashMap<>();
    /**
     * Results of Jannovar's functional annotation with respect to transcripts this variant overlaps with.
     */
    private VariantAnnotations annotations;
    /**
     * The primary graphics presented to the user for this variant.
     */
    private String graphics;
    private SplicingPredictionData primaryPrediction;

    public SplicingVariantAlleleEvaluation(VariantContext base, Allele altAllele) {
        this.base = base;
        this.altAllele = altAllele;
    }


    public String getGraphics() {
        return graphics;
    }

    public void setGraphics(String graphics) {
        this.graphics = graphics;
    }

    public void putPredictionData(String transcriptAccession, SplicingPredictionData predictionData) {
        // TODO: 1. 7. 2020 this is the place where we effectively decide about the transcript that is affected by variant the most
        //  Revise if necessary.

        this.predictionData.put(transcriptAccession, predictionData);
        primaryPrediction = this.predictionData.values().stream()
                .max(Comparator.comparing(spd -> spd.getPrediction().getMaxPathogenicity()))
                .orElse(null);
    }

    public void putAllPredictionData(Map<String, SplicingPredictionData> predictionData) {
        predictionData.forEach(this::putPredictionData);
    }

    @Override
    public VariantAnnotations getAnnotations() {
        return annotations;
    }

    public void setAnnotations(VariantAnnotations annotations) {
        this.annotations = annotations;
    }

    public VariantContext getBase() {
        return base;
    }

    public Allele getAltAllele() {
        return altAllele;
    }

    public Prediction getPredictionForTranscript(String accessionId) {
        return predictionData.get(accessionId).getPrediction();
    }

    public Map<VariantEffect, Collection<Annotation>> getAnnotationsByEffect() {
        Map<VariantEffect, Collection<Annotation>> effectMap = new TreeMap<>();
        getAnnotations().getAnnotations().forEach(ann -> ann.getEffects().forEach(eff -> {
            if (!effectMap.containsKey(eff)) {
                effectMap.put(eff, new HashSet<>());
            }
            effectMap.get(eff).add(ann);
        }));
        return effectMap;
    }

    public Double getMaxScore() {
        return primaryPrediction == null
                ? Double.NaN
                : primaryPrediction.getPrediction().getMaxPathogenicity();
    }

    @Override
    public SplicingPredictionData getPrimaryPrediction() {
        return primaryPrediction;
    }

    /**
     * Get accession ID of the <em>primary</em> transcript - the transcript with the highest reported pathogenicity.
     * We create the graphics with respect to this transcript.
     *
     * @return String with transcript accession ID or <code>null</code>
     */
    public String getPrimaryTxId() {
        return primaryPrediction == null
                ? null
                : primaryPrediction.getTranscript().getAccessionId();
    }

    public String getRepresentation() {
        return String.format("%s:%d%s>%s", base.getContig(), base.getStart(),
                base.getReference().getBaseString(),
                altAllele.getBaseString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SplicingVariantAlleleEvaluation that = (SplicingVariantAlleleEvaluation) o;
        return Objects.equals(base, that.base) &&
                Objects.equals(altAllele, that.altAllele) &&
                Objects.equals(predictionData, that.predictionData) &&
                Objects.equals(annotations, that.annotations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, altAllele, predictionData, annotations);
    }

    @Override
    public String toString() {
        return "VariantDataBox{" +
                "base=" + base +
                ", altAllele=" + altAllele +
                ", predictionData=" + predictionData +
                ", annotations=" + annotations +
                '}';
    }
}
