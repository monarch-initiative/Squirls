package org.monarchinitiative.threes.cli;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.threes.core.Metadata;
import org.monarchinitiative.threes.core.Prediction;
import org.monarchinitiative.threes.core.SplicingPredictionData;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Simple implementation of {@link SplicingPredictionData} for test purposes only.
 */
public class SimpleSplicingPredictionData implements SplicingPredictionData {

    private final GenomeVariant variant;
    private final SplicingTranscript transcript;
    private final SequenceInterval sequence;
    private final Map<String, Object> featureMap = new HashMap<>();

    private Prediction prediction;
    private Metadata metadata;

    public SimpleSplicingPredictionData(GenomeVariant variant, SplicingTranscript transcript, SequenceInterval sequence) {
        this.variant = variant;
        this.transcript = transcript;
        this.sequence = sequence;
    }


    @Override
    public Prediction getPrediction() {
        return prediction;
    }

    @Override
    public void setPrediction(Prediction prediction) {
        this.prediction = prediction;
    }

    @Override
    public GenomeVariant getVariant() {
        return variant;
    }

    @Override
    public SplicingTranscript getTranscript() {
        return transcript;
    }

    @Override
    public SequenceInterval getSequence() {
        return sequence;
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public Set<String> getFeatureNames() {
        return featureMap.keySet();
    }

    @Override
    public <T> T getFeature(String featureName, Class<T> clz) {
        return clz.cast(featureMap.get(featureName));
    }

    @Override
    public void putFeature(String name, Object value) {
        featureMap.put(name, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleSplicingPredictionData that = (SimpleSplicingPredictionData) o;
        return Objects.equals(variant, that.variant) &&
                Objects.equals(transcript, that.transcript) &&
                Objects.equals(sequence, that.sequence) &&
                Objects.equals(featureMap, that.featureMap) &&
                Objects.equals(prediction, that.prediction) &&
                Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variant, transcript, sequence, featureMap, prediction, metadata);
    }

    @Override
    public String toString() {
        return "SimpleSplicingPredictionData{" +
                "variant=" + variant +
                ", transcript=" + transcript +
                ", sequence=" + sequence +
                ", featureMap=" + featureMap +
                ", prediction=" + prediction +
                ", metadata=" + metadata +
                '}';
    }
}
