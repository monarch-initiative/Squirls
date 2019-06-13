package org.monarchinitiative.threes.core.model;

/**
 *
 */
public class SplicingExon extends SplicingRegion {


    private SplicingExon(Builder builder) {
        super(builder.begin, builder.end);
    }


    public static Builder newBuilder() {
        return new Builder();
    }


    @Override
    public String toString() {
        return "SplicingExon{" +
                "begin=" + begin +
                ", end=" + end +
                '}';
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
