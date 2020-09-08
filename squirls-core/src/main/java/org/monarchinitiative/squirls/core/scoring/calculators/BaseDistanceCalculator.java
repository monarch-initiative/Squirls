package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;

abstract class BaseDistanceCalculator implements FeatureCalculator {

    protected static int getDiff(GenomeInterval variantInterval,
                                 GenomePosition closestSite) {
        final int diff = closestSite.differenceTo(variantInterval);
        if (diff > 0) {
            // variant is upstream from the border position
            return -diff;
        } else if (diff < 0) {
            // variant is downstream from the border position
            return -diff + 1;
        } else {
            /*
            Due to representation of exon|Intron / intron|Exon boundary as a GenomePosition that represents position of
            the capital E/I character above, we need to distinguish when variant interval denotes
              - a deletion of the boundary, or
              - SNP at +1 position.

            The code below handles these situations.
            */
            if (variantInterval.contains(closestSite) && variantInterval.length() > 1) {
                // deletion of the boundary
                return 0;
            } else {
                // SNP at +1 position
                return 1;
            }
        }
    }
}
