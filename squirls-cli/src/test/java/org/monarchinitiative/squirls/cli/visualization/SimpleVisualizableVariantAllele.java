package org.monarchinitiative.squirls.cli.visualization;

import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import org.monarchinitiative.squirls.core.SplicingPredictionData;

import java.util.Map;
import java.util.Objects;

/**
 * Implementation of {@link VisualizableVariantAllele} for testing.
 */
class SimpleVisualizableVariantAllele implements VisualizableVariantAllele {

    private final VariantAnnotations annotations;

    private final Map<String, SplicingPredictionData> squirlsPredictions;

    SimpleVisualizableVariantAllele(VariantAnnotations annotations, Map<String, SplicingPredictionData> squirlsPredictions) {
        this.annotations = annotations;
        this.squirlsPredictions = squirlsPredictions;
    }

    @Override
    public VariantAnnotations variantAnnotations() {
        return annotations;
    }

    @Override
    public Map<String, SplicingPredictionData> squirlsPredictions() {
        return squirlsPredictions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleVisualizableVariantAllele that = (SimpleVisualizableVariantAllele) o;
        return Objects.equals(annotations, that.annotations) &&
                Objects.equals(squirlsPredictions, that.squirlsPredictions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotations, squirlsPredictions);
    }

    @Override
    public String toString() {
        return "SimpleVisualizableVariantAllele{" +
                "annotations=" + annotations +
                ", squirlsPredictions=" + squirlsPredictions +
                '}';
    }
}
