package org.monarchinitiative.squirls.core.reference;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;

import java.util.Optional;

/**
 * POJO for grouping location data of variant with respect to transcript.
 */
public class SplicingLocationData {

    private static final SplicingLocationData OUTSIDE = builder()
            .setSplicingPosition(SplicingPosition.OUTSIDE)
            .setExonIndex(-1)
            .setIntronIndex(-1)
            .build();

    private final SplicingPosition position;

    private final GenomePosition donorBoundary;
    private final GenomeInterval donorRegion;
    private final GenomePosition acceptorBoundary;
    private final GenomeInterval acceptorRegion;
    private final int intronIdx;
    private final int exonIdx;

    private SplicingLocationData(Builder builder) {
        intronIdx = builder.intronIdx;
        exonIdx = builder.exonIdx;
        position = builder.position;
        donorBoundary = builder.donorBoundary;
        donorRegion = builder.donorRegion;
        acceptorBoundary = builder.acceptorBoundary;
        acceptorRegion = builder.acceptorRegion;
    }

    public static SplicingLocationData outside() {
        return OUTSIDE;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<GenomePosition> getDonorBoundary() {
        return Optional.ofNullable(donorBoundary);
    }

    public Optional<GenomeInterval> getDonorRegion() {
        return Optional.ofNullable(donorRegion);
    }

    public Optional<GenomePosition> getAcceptorBoundary() {
        return Optional.ofNullable(acceptorBoundary);
    }

    public Optional<GenomeInterval> getAcceptorRegion() {
        return Optional.ofNullable(acceptorRegion);
    }

    public int getIntronIdx() {
        return intronIdx;
    }

    public int getExonIdx() {
        return exonIdx;
    }

    public SplicingPosition getPosition() {
        return position;
    }


    @Override
    public String toString() {
        return "spl pos [" + position + "i:" + intronIdx + ";e:" + exonIdx + "]";
    }

    public enum SplicingPosition {
        /**
         * Variant is located in exon and it does not change sequence of splice donor or acceptor sites.
         */
        EXON,
        /**
         * Variant is located in intron and it does not change sequence of splice donor or acceptor sites.
         */
        INTRON,

        /**
         * Variant overlaps with the splice acceptor site.
         */
        ACCEPTOR,
        /**
         * Variant overlaps with the splice donor site.
         */
        DONOR,
        /**
         * Variant does not overlap with coding region of given transcript.
         */
        OUTSIDE
    }

    public static final class Builder {

        private int intronIdx = -1;

        private int exonIdx = -1;

        private SplicingPosition position;

        private GenomePosition donorBoundary, acceptorBoundary;

        private GenomeInterval donorRegion, acceptorRegion;

        private Builder() {
        }

        public Builder setExonIndex(int exonIndex) {
            this.exonIdx = exonIndex;
            return this;
        }

        public Builder setIntronIndex(int intronIdx) {
            this.intronIdx = intronIdx;
            return this;
        }

        public Builder setSplicingPosition(SplicingPosition position) {
            this.position = position;
            return this;
        }

        public Builder setDonorBoundary(GenomePosition donorBoundary) {
            this.donorBoundary = donorBoundary;
            return this;
        }

        public Builder setDonorRegion(GenomeInterval donorRegion) {
            this.donorRegion = donorRegion;
            return this;
        }

        public Builder setAcceptorBoundary(GenomePosition acceptorBoundary) {
            this.acceptorBoundary = acceptorBoundary;
            return this;
        }

        public Builder setAcceptorRegion(GenomeInterval acceptorRegion) {
            this.acceptorRegion = acceptorRegion;
            return this;
        }

        public SplicingLocationData build() {
            return new SplicingLocationData(this);
        }
    }
}
