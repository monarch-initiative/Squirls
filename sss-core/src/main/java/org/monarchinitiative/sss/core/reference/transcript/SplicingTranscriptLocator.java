package org.monarchinitiative.sss.core.reference.transcript;

import org.monarchinitiative.sss.core.model.SplicingTranscript;
import org.monarchinitiative.sss.core.model.SplicingVariant;
import org.monarchinitiative.sss.core.reference.SplicingLocationData;

public interface SplicingTranscriptLocator {

    SplicingLocationData locate(SplicingVariant variant, SplicingTranscript transcript);
}
