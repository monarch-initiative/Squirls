package org.monarchinitiative.threes.cli.cmd.analyze_vcf;

import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.monarchinitiative.threes.core.Prediction;
import org.monarchinitiative.threes.core.SplicingPredictionData;

import java.util.*;

/**
 * This class is a POJO for a single ALT allele of the variant.
 */
public class SplicingVariantAlleleEvaluation {

    /**
     * The base variant context that is being analyzed.
     */
    private final VariantContext base;

    /**
     * The ALT allele of the variant context that is being analyzed.
     */
    private final Allele altAllele;
    /**
     * Results of the splicing analysis.
     */
    private final Map<String, SplicingPredictionData> predictionData = new HashMap<>();
    /**
     * Results of Jannovar's functional annotation with respect to transcripts this variant overlaps with.
     */
    private VariantAnnotations annotations;

    public SplicingVariantAlleleEvaluation(VariantContext base, Allele altAllele) {
        this.base = base;
        this.altAllele = altAllele;
    }

    public Map<String, SplicingPredictionData> getPredictionData() {
        return predictionData;
    }

    public void putPredictionData(String transcriptAccession, SplicingPredictionData predictionData) {
        this.predictionData.put(transcriptAccession, predictionData);
    }

    public void putAllPredictionData(Map<String, SplicingPredictionData> predictionData) {
        this.predictionData.putAll(predictionData);
    }

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
        return predictionData.values().stream()
                .map(SplicingPredictionData::getPrediction)
                .mapToDouble(Prediction::getMaxPathogenicity)
                .max()
                .orElse(Double.NaN);
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
