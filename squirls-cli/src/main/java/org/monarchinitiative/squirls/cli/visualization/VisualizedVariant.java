package org.monarchinitiative.squirls.cli.visualization;

import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import org.monarchinitiative.squirls.core.SplicingPredictionData;

/**
 * This interface specifies what an instance must meet in order to be usable for {@link SplicingVariantGraphicsGenerator}
 * for  graphics generation.
 */
public interface VisualizedVariant {

    SplicingPredictionData getPrimaryPrediction();

    VariantAnnotations getAnnotations();

}
