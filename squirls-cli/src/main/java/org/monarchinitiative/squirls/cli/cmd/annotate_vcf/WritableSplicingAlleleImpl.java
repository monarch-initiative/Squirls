package org.monarchinitiative.squirls.cli.cmd.annotate_vcf;

import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.monarchinitiative.squirls.cli.writers.WritableSplicingAllele;
import org.monarchinitiative.squirls.core.SplicingPredictionData;

import java.util.Map;
import java.util.Objects;

class WritableSplicingAlleleImpl implements WritableSplicingAllele {

    private final VariantContext variantContext;
    private final Allele allele;
    private final VariantAnnotations annotations;
    private final Map<String, SplicingPredictionData> squirlsScores;

    WritableSplicingAlleleImpl(VariantContext variantContext,
                               Allele allele,
                               VariantAnnotations annotations,
                               Map<String, SplicingPredictionData> squirlsScores) {
        this.variantContext = variantContext;
        this.allele = allele;
        this.annotations = annotations;
        this.squirlsScores = squirlsScores;
    }

    @Override
    public Allele allele() {
        return allele;
    }

    @Override
    public VariantContext variantContext() {
        return variantContext;
    }

    @Override
    public VariantAnnotations variantAnnotations() {
        return annotations;
    }

    @Override
    public Map<String, SplicingPredictionData> squirlsPredictions() {
        return squirlsScores;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WritableSplicingAlleleImpl that = (WritableSplicingAlleleImpl) o;
        return Objects.equals(variantContext, that.variantContext) &&
                Objects.equals(allele, that.allele) &&
                Objects.equals(annotations, that.annotations) &&
                Objects.equals(squirlsScores, that.squirlsScores);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variantContext, allele, annotations, squirlsScores);
    }

    @Override
    public String toString() {
        return "SplicingVariantAlleleEvaluationImpl{" +
                "variantContext=" + variantContext +
                ", allele=" + allele +
                ", annotations=" + annotations +
                ", squirlsScores=" + squirlsScores +
                '}';
    }
}
