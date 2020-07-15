package org.monarchinitiative.squirls.core.scoring.calculators;


import java.util.Map;

public class Hexamer extends BaseKmer {

    /**
     * Since we are working with hexamers, the padding is 5bp. The remaining bp is coming from REF/ALT alleles.
     */
    private static final int PADDING = 5;

    public Hexamer(Map<String, Double> kmerMap) {
        super(kmerMap);
    }

    @Override
    protected int getPadding() {
        return PADDING;
    }
}
