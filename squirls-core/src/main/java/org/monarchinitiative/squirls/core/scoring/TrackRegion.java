package org.monarchinitiative.squirls.core.scoring;

import de.charite.compbio.jannovar.reference.GenomeInterval;

public interface TrackRegion<T> {

    GenomeInterval getInterval();

    T getValue();

}
