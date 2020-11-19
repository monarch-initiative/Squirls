package org.monarchinitiative.squirls.cli.writers;

/**
 * Container for storing statistics of the analysis.
 */
public class AnalysisStats {

    private final int allVariants;
    private final int alleleCount;
    private final int annotatedAlleleCount;

    public AnalysisStats(int allVariants, int alleleCount, int annotatedAlleleCount) {
        this.allVariants = allVariants;
        this.alleleCount = alleleCount;
        this.annotatedAlleleCount = annotatedAlleleCount;
    }


    public int allVariants() {
        return allVariants;
    }

    public int alleleCount() {
        return alleleCount;
    }

    public int annotatedAlleleCount() {
        return annotatedAlleleCount;
    }
}
