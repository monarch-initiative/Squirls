package org.monarchinitiative.threes.core.reference;

/**
 * POJO for grouping location data of variant with respect to transcript.
 */
public class SplicingLocationData {

    private static final SplicingLocationData OUTSIDE = newBuilder()
            .setSplicingPosition(SplicingPosition.OUTSIDE)
            .setExonIndex(-1)
            .setIntronIndex(-1)
            .build();

    private final SplicingPosition position;

    private final int intronIdx;

    private final int exonIdx;

    private SplicingLocationData(Builder builder) {
        intronIdx = builder.intronIdx;
        exonIdx = builder.exonIdx;
        position = builder.position;
    }

    public static SplicingLocationData outside() {
        return OUTSIDE;
    }

    public static Builder newBuilder() {
        return new Builder();
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SplicingLocationData)) return false;

        SplicingLocationData that = (SplicingLocationData) o;

        if (intronIdx != that.intronIdx) return false;
        if (exonIdx != that.exonIdx) return false;
        return position == that.position;

    }

    @Override
    public int hashCode() {
        int result = position != null ? position.hashCode() : 0;
        result = 31 * result + intronIdx;
        result = 31 * result + exonIdx;
        return result;
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
         * Variant is located in intron and  it does not change sequence of splice donor or acceptor sites.
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

        public SplicingLocationData build() {
            return new SplicingLocationData(this);
        }
    }
}
