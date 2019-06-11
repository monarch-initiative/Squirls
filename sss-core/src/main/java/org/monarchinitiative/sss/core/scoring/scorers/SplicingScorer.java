package org.monarchinitiative.sss.core.scoring.scorers;

import org.monarchinitiative.sss.core.model.SequenceInterval;
import org.monarchinitiative.sss.core.model.SplicingRegion;
import org.monarchinitiative.sss.core.model.SplicingVariant;

@FunctionalInterface
public interface SplicingScorer<T extends SplicingRegion> {

    double score(SplicingVariant variant, T region, SequenceInterval sequenceInterval);

}
