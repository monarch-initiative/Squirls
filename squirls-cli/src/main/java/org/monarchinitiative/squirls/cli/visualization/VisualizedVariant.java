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
public interface VisualizedVariant {

    default GenomeVariant getVariant() {
        return getAnnotations().getGenomeVariant();
    }

    VariantAnnotations getAnnotations();

    Map<String, SplicingPredictionData> getSplicingPredictions();

    default SplicingPredictionData getPrimaryPrediction() {
        return getSplicingPredictions().values().stream()
                .max(Comparator.comparing(d -> d.getPrediction().getMaxPathogenicity()))
                .stream().findFirst()
                .orElse(SplicingPredictionData.emptyPredictionData());
    }

}
