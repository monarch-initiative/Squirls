package org.monarchinitiative.squirls.core.model;

import de.charite.compbio.jannovar.reference.GenomeInterval;

import java.util.Objects;

/**
 *
 */
public abstract class SplicingRegion {

    protected final GenomeInterval interval;

    protected SplicingRegion(GenomeInterval interval) {
        this.interval = interval;
    }

    public GenomeInterval getInterval() {
        return interval;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SplicingRegion that = (SplicingRegion) o;
        return Objects.equals(interval, that.interval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interval);
    }

    @Override
    public String toString() {
        return "SplicingRegion{" +
                "interval=" + interval +
                '}';
    }
}
