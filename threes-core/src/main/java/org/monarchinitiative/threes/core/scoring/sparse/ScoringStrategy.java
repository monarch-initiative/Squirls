package org.monarchinitiative.threes.core.scoring.sparse;

import java.util.EnumSet;
import java.util.Set;

// TODO - make package-private scope
public enum ScoringStrategy {

    CANONICAL_DONOR,
    CRYPTIC_DONOR,
    CRYPTIC_DONOR_IN_CANONICAL_POSITION,

    CANONICAL_ACCEPTOR,
    CRYPTIC_ACCEPTOR,
    CRYPTIC_ACCEPTOR_IN_CANONICAL_POSITION,

    SMS;

    public static Set<ScoringStrategy> crypticAndCanonicalDonorAndAcceptor() {
        return EnumSet.of(CANONICAL_DONOR, CRYPTIC_DONOR, CRYPTIC_DONOR_IN_CANONICAL_POSITION,
                CANONICAL_ACCEPTOR, CRYPTIC_ACCEPTOR, CRYPTIC_ACCEPTOR_IN_CANONICAL_POSITION);
    }
}