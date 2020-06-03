package org.monarchinitiative.threes.cli.cmd.analyze;

/**
 * Container for storing statistics of the analysis.
 */
public class AnalysisStats {

    private final int allVariants;
    private final int alleleCount;
    private final int annotatedAlleleCount;
    private final int pathogenicAlleleCount;

    private AnalysisStats(Builder builder) {
        allVariants = builder.allVariants;
        alleleCount = builder.alleleCount;
        annotatedAlleleCount = builder.annotatedAlleleCount;
        pathogenicAlleleCount = builder.pathogenicAlleleCount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getAllVariants() {
        return allVariants;
    }

    public int getAlleleCount() {
        return alleleCount;
    }

    public int getAnnotatedAlleleCount() {
        return annotatedAlleleCount;
    }

    public int getPathogenicAlleleCount() {
        return pathogenicAlleleCount;
    }

    public static final class Builder {
        private int allVariants;
        private int alleleCount;
        private int annotatedAlleleCount;
        private int pathogenicAlleleCount;

        private Builder() {
        }

        public Builder allVariants(int allVariants) {
            this.allVariants = allVariants;
            return this;
        }

        public Builder alleleCount(int alleleCount) {
            this.alleleCount = alleleCount;
            return this;
        }

        public Builder annotatedAlleleCount(int annotatedAlleleCount) {
            this.annotatedAlleleCount = annotatedAlleleCount;
            return this;
        }

        public Builder pathogenicAlleleCount(int pathogenicAlleleCount) {
            this.pathogenicAlleleCount = pathogenicAlleleCount;
            return this;
        }

        public AnalysisStats build() {
            return new AnalysisStats(this);
        }
    }
}
