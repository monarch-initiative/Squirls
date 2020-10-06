package org.monarchinitiative.squirls.core.scoring;

import de.charite.compbio.jannovar.reference.GenomeInterval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FloatRegion implements TrackRegion<List<Float>> {

    private final GenomeInterval interval;

    private final List<Float> value;

    private FloatRegion(GenomeInterval interval, List<Float> value) {
        this.interval = interval;
        this.value = value;
    }

    public static FloatRegion of(GenomeInterval interval, List<Float> value) {
        return new FloatRegion(interval, value);
    }

    @Override
    public GenomeInterval getInterval() {
        return interval;
    }

    @Override
    public List<Float> getValue() {
        return value;
    }

    /**
     * Get values for coordinates of {@code other} interval.
     *
     * @param other coordinates of the other interval
     * @return list with values, the list is empty if the {@code other} interval is not contained within this region
     */
    public List<Float> getValuesForInterval(GenomeInterval other) {
        if (interval.contains(other)) {
            final GenomeInterval onStrand = other.withStrand(interval.getStrand());
            final int begin = onStrand.getBeginPos() - interval.getBeginPos();
            final int end = begin + other.length();
            final List<Float> selected = value.subList(begin, end);

            if (other.getStrand().equals(interval.getStrand())) {
                return List.copyOf(selected);
            } else {
                final ArrayList<Float> list = new ArrayList<>(selected);
                Collections.reverse(list);
                return List.copyOf(list);
            }
        } else {
            return List.of();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FloatRegion that = (FloatRegion) o;
        return Objects.equals(interval, that.interval) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interval, value);
    }

    @Override
    public String toString() {
        return "FloatRegion{" +
                "interval=" + interval +
                ", value=" + value +
                '}';
    }
}
