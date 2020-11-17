package org.monarchinitiative.squirls.core;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

/**
 * Splicing prediction data where {@link SequenceInterval} is not included.
 */
public class NoRefSplicingPredictionData extends StandardSplicingPredictionData {

    protected NoRefSplicingPredictionData(GenomeVariant variant, SplicingTranscript transcript) {
        super(variant, transcript, SequenceInterval.empty());
    }

    public static NoRefSplicingPredictionData of(GenomeVariant variant, SplicingTranscript transcript) {
        return new NoRefSplicingPredictionData(variant, transcript);
    }

    /**
     * Make a copy of provided {@link SplicingPredictionData} while dropping {@link SequenceInterval}.
     *
     * @param data to copy
     * @return copy of the
     */
    public static <T extends SplicingPredictionData> NoRefSplicingPredictionData copyOf(T data) {
        NoRefSplicingPredictionData copy = new NoRefSplicingPredictionData(data.getVariant(), data.getTranscript());

        copy.putAllFeatures(data.getFeatureMap());
        copy.setPrediction(data.getPrediction());
        copy.setMetadata(data.getMetadata());

        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "NoRefSplicingPredictionData{" +
                "variant=" + getVariant() +
                ", transcript=" + getTranscript() +
                ", featureMap=" + getFeatureMap() +
                ", prediction=" + getPrediction() +
                ", metadata=" + getMetadata() +
                '}';
    }


}
