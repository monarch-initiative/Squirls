package org.monarchinitiative.squirls.core.scoring;

import de.charite.compbio.jannovar.reference.GenomeInterval;

import java.util.Arrays;
import java.util.Objects;

public class FloatRegion implements TrackRegion<float[]> {

    private final GenomeInterval interval;

    private final float[] value;

    private FloatRegion(GenomeInterval interval, float[] value) {
        this.interval = interval;
        this.value = value;
    }

    public static FloatRegion of(GenomeInterval interval, float[] value) {
        return new FloatRegion(interval, value);
    }

    @Override
    public GenomeInterval getInterval() {
        return interval;
    }

    /**
     * Reverse an array in place.
     *
     * @param array to be reversed
     * @return reversed array, such that the first array value is moved to the last.
     */
    private static float[] reverseArray(float[] array) {
        final int l = array.length / 2;
        int j;
        float val;
        for (int i = 0; i < l; i++) {
            j = array.length - i - 1;
            val = array[i];
            array[i] = array[j];
            array[j] = val;
        }
        return array;
    }

    @Override
    public float[] getValue() {
        return value;
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

    /**
     * Get values for coordinates of {@code other} interval.
     *
     * @param other coordinates of the other interval
     * @return list with values, the list is empty if the {@code other} interval is not contained within this region
     */
    public float[] getValuesForInterval(GenomeInterval other) {
        if (interval.contains(other)) {
            final GenomeInterval onStrand = other.withStrand(interval.getStrand());
            final int begin = onStrand.getBeginPos() - interval.getBeginPos();
            final int end = begin + other.length();
            final float[] selected = Arrays.copyOfRange(value, begin, end);

            if (other.getStrand().equals(interval.getStrand())) {
                return selected;
            } else {
                return reverseArray(selected);
            }
        } else {
            return new float[0];
        }
    }
}
