package org.monarchinitiative.squirls.core;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.classifier.Prediction;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.Set;

class EmptySplicingPredictionData implements SplicingPredictionData {

    private static final EmptySplicingPredictionData INSTANCE = new EmptySplicingPredictionData();

    private EmptySplicingPredictionData() {
        // private no-op
    }

    public static EmptySplicingPredictionData getInstance() {
        return INSTANCE;
    }

    @Override
    public Prediction getPrediction() {
        return Prediction.emptyPrediction();
    }

    @Override
    public void setPrediction(Prediction prediction) {
        // no-op
    }

    @Override
    public GenomeVariant getVariant() {
        return null;
    }

    @Override
    public SplicingTranscript getTranscript() {
        return SplicingTranscript.getDefaultInstance();
    }

    @Override
    public SequenceInterval getSequence() {
        return null;
    }

    @Override
    public Metadata getMetadata() {
        return Metadata.empty();
    }

    @Override
    public void setMetadata(Metadata metadata) {
        // no-op
    }

    @Override
    public Set<String> getFeatureNames() {
        return Set.of();
    }

    @Override
    public <T> T getFeature(String featureName, Class<T> clz) {
        return null;
    }

    @Override
    public void putFeature(String name, Object value) {
        // no-op
    }
}
