package org.monarchinitiative.threes.core.scoring;

import java.util.Map;

/**
 * This scorer TODO - add docs
 */
public class SeptamerFeatureCalculator extends KmerFeatureCalculator {

    /**
     * Since we are working with septamers, the padding is 6bp. The remaining bp is coming from REF/ALT alleles.
     */
    private static final int PADDING = 6;

    public SeptamerFeatureCalculator(Map<String, Double> septamerMap) {
        super(septamerMap);
    }

    @Override
    protected int getPadding() {
        return PADDING;
    }

}
