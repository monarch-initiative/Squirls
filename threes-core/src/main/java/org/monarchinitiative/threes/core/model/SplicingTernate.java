package org.monarchinitiative.threes.core.model;

/**
 *
 */
public class SplicingTernate {

    private final SplicingVariant variant;

    private final SplicingRegion region;

    private final SequenceInterval sequenceInterval;

    private SplicingTernate(SplicingVariant variant, SplicingRegion region, SequenceInterval sequenceInterval) {
        this.variant = variant;
        this.region = region;
        this.sequenceInterval = sequenceInterval;
    }

    public static SplicingTernate of(SplicingVariant variant, SplicingRegion region, SequenceInterval sequenceInterval) {
        return new SplicingTernate(variant, region, sequenceInterval);
    }

    public SplicingVariant getVariant() {
        return variant;
    }

    public SplicingRegion getRegion() {
        return region;
    }

    public SequenceInterval getSequenceInterval() {
        return sequenceInterval;
    }

    @Override
    public String toString() {
        return "SplicingTernate{" +
                "variant=" + variant +
                ", region=" + region +
                ", sequenceInterval=" + sequenceInterval +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SplicingTernate)) return false;

        SplicingTernate that = (SplicingTernate) o;

        if (variant != null ? !variant.equals(that.variant) : that.variant != null) return false;
        if (region != null ? !region.equals(that.region) : that.region != null) return false;
        return sequenceInterval != null ? sequenceInterval.equals(that.sequenceInterval) : that.sequenceInterval == null;

    }

    @Override
    public int hashCode() {
        int result = variant != null ? variant.hashCode() : 0;
        result = 31 * result + (region != null ? region.hashCode() : 0);
        result = 31 * result + (sequenceInterval != null ? sequenceInterval.hashCode() : 0);
        return result;
    }
}