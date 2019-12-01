package org.monarchinitiative.threes.core.reference.transcript;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.reference.SplicingLocationData;

public interface SplicingTranscriptLocator {

    /**
     * Locate {@code variant} on given {@code transcript}.
     *
     * @param variant    variant
     * @param transcript transcript
     * @return location data
     */
    SplicingLocationData locate(GenomeVariant variant, SplicingTranscript transcript);


}
