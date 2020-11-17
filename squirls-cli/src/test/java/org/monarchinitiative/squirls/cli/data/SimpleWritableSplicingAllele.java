package org.monarchinitiative.squirls.cli.data;

import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.monarchinitiative.squirls.cli.writers.WritableSplicingAllele;
import org.monarchinitiative.squirls.core.SplicingPredictionData;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
class SimpleWritableSplicingAllele implements WritableSplicingAllele {

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

    SimpleWritableSplicingAllele(VariantContext base, Allele altAllele) {
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
        this.predictionData.put(transcriptAccession, predictionData);
    }

    public void putAllPredictionData(Map<String, SplicingPredictionData> predictionData) {
        predictionData.forEach(this::putPredictionData);
    }

    @Override
    public Allele allele() {
        return altAllele;
    }

    @Override
    public VariantContext variantContext() {
        return base;
    }

    @Override
    public VariantAnnotations variantAnnotations() {
        return annotations;
    }

    public void setAnnotations(VariantAnnotations annotations) {
        this.annotations = annotations;
    }

    @Override
    public Map<String, SplicingPredictionData> squirlsPredictions() {
        return predictionData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleWritableSplicingAllele that = (SimpleWritableSplicingAllele) o;
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
