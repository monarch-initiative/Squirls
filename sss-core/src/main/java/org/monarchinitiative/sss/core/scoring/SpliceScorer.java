package org.monarchinitiative.sss.core.scoring;

import org.monarchinitiative.sss.core.model.SequenceInterval;
import org.monarchinitiative.sss.core.model.SplicingTranscript;
import org.monarchinitiative.sss.core.model.SplicingVariant;

public interface SpliceScorer {

    double score(SplicingVariant variant, SplicingTranscript transcript, SequenceInterval sequenceInterval);

}
