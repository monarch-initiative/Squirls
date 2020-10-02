package org.monarchinitiative.squirls.core.model;

import de.charite.compbio.jannovar.reference.GenomeInterval;

import java.util.List;

public interface ConservationInterval {

    GenomeInterval getInterval();

    List<Float> getConservation();

}
