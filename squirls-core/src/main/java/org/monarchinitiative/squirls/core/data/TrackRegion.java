package org.monarchinitiative.squirls.core.data;

import de.charite.compbio.jannovar.reference.GenomeInterval;

import java.util.List;

public interface TrackRegion<K> {

    List<K> getValues(GenomeInterval interval);
}
