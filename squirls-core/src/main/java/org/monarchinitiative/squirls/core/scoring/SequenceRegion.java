package org.monarchinitiative.squirls.core.scoring;

import de.charite.compbio.jannovar.reference.GenomeInterval;

import java.util.Objects;

public class SequenceRegion implements TrackRegion<String> {

    /*
    For now, this is just a dumb data container. However, consider replacing SequenceInterval with this class.
     */

    private final GenomeInterval interval;

    private final String value;

    private SequenceRegion(GenomeInterval interval, String value) {
        this.interval = interval;
        this.value = value;
    }

    public static SequenceRegion of(GenomeInterval interval, String value) {
        return new SequenceRegion(interval, value);
    }

    @Override
    public GenomeInterval getInterval() {
        return interval;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SequenceRegion that = (SequenceRegion) o;
        return Objects.equals(interval, that.interval) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interval, value);
    }

    @Override
    public String toString() {
        return "SequenceRegion{" +
                "interval=" + interval +
                ", value='" + value + '\'' +
                '}';
    }
}
