package org.monarchinitiative.squirls.core;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.scoring.Annotatable;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SimpleAnnotatable implements Annotatable {

    private final GenomeVariant variant;
    private final SplicingTranscript transcript;
    private final SequenceInterval sequence;
    private final Map<String, Object> features = new HashMap<>();
    private Metadata metadata;

    public SimpleAnnotatable(GenomeVariant variant, SplicingTranscript transcript, SequenceInterval sequence) {
        this.variant = variant;
        this.transcript = transcript;
        this.sequence = sequence;
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
        return features.keySet();
    }

    @Override
    public <T> T getFeature(String featureName, Class<T> clz) {
        return clz.cast(features.get(featureName));
    }

    @Override
    public void putFeature(String name, Object value) {
        features.put(name, value);
    }
}
