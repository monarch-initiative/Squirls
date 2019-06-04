package org.monarchinitiative.sss.core.model;

/**
 *
 */
public class SplicingExon {

    private final int begin;

    private final int end;

    private SplicingExon(Builder builder) {
        begin = builder.begin;
        end = builder.end;
    }

    public static Builder newBuilder() {
        return new Builder();
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
        if (!(o instanceof SplicingExon)) return false;

        SplicingExon that = (SplicingExon) o;

        if (begin != that.begin) return false;
        return end == that.end;

    }

    @Override
    public int hashCode() {
        int result = begin;
        result = 31 * result + end;
        return result;
    }

    public static final class Builder {

        private int begin;

        private int end;

        private Builder() {
        }

        public Builder setBegin(int begin) {
            this.begin = begin;
            return this;
        }

        public Builder setEnd(int end) {
            this.end = end;
            return this;
        }

        public SplicingExon build() {
            return new SplicingExon(this);
        }
    }
}
