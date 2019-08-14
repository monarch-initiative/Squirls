package org.monarchinitiative.threes.core.model;

import org.monarchinitiative.threes.core.reference.fasta.InvalidCoordinatesException;

/**
 * POJO for {@link SplicingVariant}, {@link SplicingRegion} (either exon or intron), and {@link SequenceInterval}, all
 * adjusted to strand of the transcript.
 */
public class SplicingTernate {

    private final SplicingVariant variant;

    private final SplicingRegion region;

    private final SequenceInterval sequenceInterval;

    private SplicingTernate(SplicingVariant variant, SplicingRegion region, SequenceInterval sequenceInterval) {
        if (variant.getCoordinates().getStrand() != sequenceInterval.getCoordinates().getStrand()) {
            throw new InvalidCoordinatesException(String.format("Sequence %s and variant %s are not on the same strand", sequenceInterval, variant));
        }

        this.variant = variant;
        this.region = region;
        this.sequenceInterval = sequenceInterval;

    }

    /**
     * Make the ternate from variant, region, and sequence interval - all of them must be on the same chromosome strand.
     *
     * @param variant          {@link SplicingVariant} variant
     * @param region           {@link SplicingRegion} region
     * @param sequenceInterval {@link SequenceInterval} sequence interval
     * @return ternate
     */
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
