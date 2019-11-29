package org.monarchinitiative.threes.core.model;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

/**
 * POJO for {@link GenomeVariant}, {@link SplicingRegion} (either exon or intron), and {@link SequenceInterval}, all
 * adjusted to strand of the transcript.
 */
public class SplicingTernate {

    private final GenomeVariant variant;

    private final SplicingRegion region;

    private final SequenceInterval sequenceInterval;

    private SplicingTernate(GenomeVariant variant, SplicingRegion region, SequenceInterval sequenceInterval) {
        if (variant.getChr() != region.getInterval().getChr() || variant.getChr() != sequenceInterval.getInterval().getChr()) {
            throw new IllegalArgumentException(String.format("Creating ternate from data on different contigs - variant: `%d`, region: `%d`, sequence: `%d`",
                    variant.getChr(), region.getInterval().getChr(), sequenceInterval.getInterval().getChr()));
        }
        this.variant = variant;
        this.region = region;
        this.sequenceInterval = sequenceInterval;

    }

    /**
     * Make the ternate from variant, region, and sequence interval - all of them must be on the same chromosome strand.
     *
     * @param variant          {@link GenomeVariant} variant
     * @param region           {@link SplicingRegion} region
     * @param sequenceInterval {@link SequenceInterval} sequence interval
     * @return ternate
     */
    public static SplicingTernate of(GenomeVariant variant, SplicingRegion region, SequenceInterval sequenceInterval) {
        return new SplicingTernate(variant, region, sequenceInterval);
    }

    public GenomeVariant getVariant() {
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
