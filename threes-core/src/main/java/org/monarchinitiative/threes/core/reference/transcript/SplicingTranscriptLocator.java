package org.monarchinitiative.threes.core.reference.transcript;

import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.model.SplicingVariant;
import org.monarchinitiative.threes.core.reference.SplicingLocationData;

public interface SplicingTranscriptLocator {

    /**
     * Locate {@code variant} on given {@code transcript}.
     *
     * <p>
     * <b>IMPORTANT</b> - {@code variant} must be on the same strand as the {@code transcript}!
     *
     * @param variant    variant
     * @param transcript transcript
     * @return location data
     */
    SplicingLocationData locate(SplicingVariant variant, SplicingTranscript transcript);


}
