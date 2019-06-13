package org.monarchinitiative.threes.core.model;

/**
 *
 */
public abstract class SplicingRegion {

    protected final int begin;

    protected final int end;

    protected SplicingRegion(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SplicingRegion)) return false;

        SplicingRegion that = (SplicingRegion) o;

        if (begin != that.begin) return false;
        return end == that.end;

    }

    @Override
    public int hashCode() {
        int result = begin;
        result = 31 * result + end;
        return result;
    }
}
