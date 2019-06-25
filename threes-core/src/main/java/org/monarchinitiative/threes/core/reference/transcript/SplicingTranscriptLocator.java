package org.monarchinitiative.threes.core.reference.transcript;

import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.model.SplicingVariant;
import org.monarchinitiative.threes.core.reference.SplicingLocationData;

public interface SplicingTranscriptLocator {

    SplicingLocationData locate(SplicingVariant variant, SplicingTranscript transcript);


}
