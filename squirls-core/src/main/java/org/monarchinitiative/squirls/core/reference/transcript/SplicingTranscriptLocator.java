package org.monarchinitiative.squirls.core.reference.transcript;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.reference.SplicingLocationData;

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
