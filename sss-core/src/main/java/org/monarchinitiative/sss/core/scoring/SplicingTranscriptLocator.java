package org.monarchinitiative.sss.core.scoring;

import org.monarchinitiative.sss.core.model.SplicingTranscript;
import org.monarchinitiative.sss.core.model.SplicingVariant;
import org.monarchinitiative.sss.core.reference.SplicingLocationData;

public interface SplicingTranscriptLocator {

    SplicingLocationData localize(SplicingVariant variant, SplicingTranscript transcript);
}
