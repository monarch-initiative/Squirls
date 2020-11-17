package org.monarchinitiative.squirls.cli.visualization;

import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.SplicingPredictionData;

import java.util.Comparator;
import java.util.Map;

/**
 * This interface specifies what an instance must meet in order to be usable for {@link SplicingVariantGraphicsGenerator}
 * for graphics generation.
 */
public interface VisualizableVariantAllele {

    /**
     * @return result of Jannovar's annotation
     */
    VariantAnnotations variantAnnotations();

    /**
     * Get map with Squirls predictions wrt. overlapping transcripts
     *
     * @return map where key -> transcript accession, value -> Squirls predictions for the transcript
     */
    Map<String, SplicingPredictionData> squirlsPredictions();

    default GenomeVariant genomeVariant() {
        return variantAnnotations().getGenomeVariant();
    }

    default SplicingPredictionData getPrimaryPrediction() {
        return squirlsPredictions().values().stream()
                .max(Comparator.comparing(spd -> spd.getPrediction().getMaxPathogenicity()))
                .orElse(SplicingPredictionData.emptyPredictionData());
    }

}
