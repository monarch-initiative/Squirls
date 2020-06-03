package org.monarchinitiative.threes.cli.cmd.analyze;

import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.monarchinitiative.threes.core.classifier.Prediction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class is a POJO for a single ALT allele of the variant.
 */
public class VariantDataBox {

    private final VariantContext base;
    private final Allele altAllele;
    private final Map<TranscriptModel, Prediction> predictionMap = new HashMap<>();
    private VariantAnnotations annotations;

    public VariantDataBox(VariantContext base, Allele altAllele) {
        this.base = base;
        this.altAllele = altAllele;
    }

    public VariantAnnotations getAnnotations() {
        return annotations;
    }

    public void setAnnotations(VariantAnnotations annotations) {
        this.annotations = annotations;
    }

    public void putPrediction(TranscriptModel transcript, Prediction prediction) {
        this.predictionMap.put(transcript, prediction);
    }

    public void putAllPredictions(Map<TranscriptModel, Prediction> predictionMap) {
        this.predictionMap.putAll(predictionMap);
    }

    public Map<TranscriptModel, Prediction> getPredictionMap() {
        return predictionMap;
    }

    public List<Annotation> getTranscriptAnnotations() {
        return annotations.getAnnotations();
    }

    public VariantContext getBase() {
        return base;
    }

    public Allele getAltAllele() {
        return altAllele;
    }

    public Double getMaxScore() {
        return predictionMap.values().stream()
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
        VariantDataBox that = (VariantDataBox) o;
        return Objects.equals(base, that.base) &&
                Objects.equals(altAllele, that.altAllele) &&
                Objects.equals(predictionMap, that.predictionMap) &&
                Objects.equals(annotations, that.annotations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, altAllele, predictionMap, annotations);
    }

    @Override
    public String toString() {
        return "VariantDataBox{" +
                "base=" + base +
                ", altAllele=" + altAllele +
                ", predictionMap=" + predictionMap +
                ", annotations=" + annotations +
                '}';
    }
}
