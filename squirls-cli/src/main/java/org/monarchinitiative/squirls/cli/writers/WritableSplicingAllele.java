package org.monarchinitiative.squirls.cli.writers;

import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.monarchinitiative.squirls.core.SplicingPredictionData;

import java.util.Comparator;
import java.util.Map;


/**
 * Result of variant annotation pipeline for a single splicing variant allele. The {@link WritableSplicingAllele} is
 * a single entry point into result writers.
 * <p>
 * Note that although {@link VariantContext} can contain multiple <em>alt</em> alleles, this object represents only
 * a single <em>allele</em>.
 */
public interface WritableSplicingAllele {

    /**
     * @return alt allele
     */
    Allele allele();

    /**
     * @return variant context representing the input variant
     */
    VariantContext variantContext();


    VariantAnnotations variantAnnotations();


    /**
     * Get map with Squirls predictions wrt. overlapping transcripts
     *
     * @return map where key -> transcript accession, value -> Squirls predictions for the transcript
     */
    Map<String, SplicingPredictionData> squirlsPredictions();


    default SplicingPredictionData getPrimaryPrediction() {
        return squirlsPredictions().values().stream()
                .max(Comparator.comparing(spd -> spd.getPrediction().getMaxPathogenicity()))
                .orElse(SplicingPredictionData.emptyPredictionData());
    }

    /**
     * @return max Squirls score or {@link Double#NaN} if no prediction has been made
     */
    default double maxSquirlsScore() {
        return squirlsPredictions().values().stream()
                .map(e -> e.getPrediction().getMaxPathogenicity())
                .max(Double::compareTo)
                .orElse(Double.NaN);
    }

}
