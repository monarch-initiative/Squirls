package org.monarchinitiative.sss.core.reference;

/**
 * POJO for grouping location data of variant with respect to transcript.
 */
public class SplicingLocationData {

    private static final SplicingLocationData OUTSIDE = newBuilder()
            .setSplicingPosition(SplicingPosition.OUTSIDE)
            .setFeatureIndex(-1)
            .build();

    private final SplicingPosition position;

    private final int featureIndex;

    private SplicingLocationData(Builder builder) {
        featureIndex = builder.featureIndex;
        position = builder.position;
    }


    public static SplicingLocationData outside() {
        return OUTSIDE;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public SplicingPosition getPosition() {
        return position;
    }

    public int getFeatureIndex() {
        return featureIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SplicingLocationData)) return false;

        SplicingLocationData that = (SplicingLocationData) o;

        if (featureIndex != that.featureIndex) return false;
        return position == that.position;

    }

    @Override
    public int hashCode() {
        int result = position != null ? position.hashCode() : 0;
        result = 31 * result + featureIndex;
        return result;
    }

    @Override
    public String toString() {
        return "spl pos [" + position + " feature " + featureIndex + "]";
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

        private int featureIndex;

        private SplicingPosition position;

        private Builder() {
        }


        public Builder setFeatureIndex(int featureIndex) {
            this.featureIndex = featureIndex;
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
