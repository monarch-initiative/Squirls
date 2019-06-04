package org.monarchinitiative.sss.core.model;

/**
 *
 */
public class GenomeInterval {

    private final String contig;

    private final int begin, end;

    private final boolean strand;

    private final int contigLength;

    private GenomeInterval(Builder builder) {
        contig = builder.contig;
        begin = builder.begin;
        end = builder.end;
        strand = builder.strand;
        contigLength = builder.contigLength;
    }

    public static Builder newBuilder() {

        return new Builder();
    }

    public static Builder newBuilder(GenomeInterval interval) {
        return newBuilder()
                .setContig(interval.contig)
                .setBegin(interval.begin)
                .setEnd(interval.end)
                .setStrand(interval.strand)
                .setContigLength(interval.contigLength);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GenomeInterval)) return false;

        GenomeInterval that = (GenomeInterval) o;

        if (begin != that.begin) return false;
        if (end != that.end) return false;
        if (strand != that.strand) return false;
        if (contigLength != that.contigLength) return false;
        return contig != null ? contig.equals(that.contig) : that.contig == null;

    }

    @Override
    public int hashCode() {
        int result = contig != null ? contig.hashCode() : 0;
        result = 31 * result + begin;
        result = 31 * result + end;
        result = 31 * result + (strand ? 1 : 0);
        result = 31 * result + contigLength;
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s:[%d-%d](%d)%s", contig, begin, end, contigLength, strand);
    }

    public String getContig() {
        return contig;
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }

    public int getLength() {
        return end - begin;
    }

    public boolean isStrand() {
        return strand;
    }

    public int getContigLength() {
        return contigLength;
    }

    public GenomeInterval withStrand(boolean strand) {
        final int beginOnStrand, endOnStrand;
        final String sequenceOnStrand;
        if (strand == this.isStrand()) {
            beginOnStrand = begin;
            endOnStrand = end;
        } else {
            beginOnStrand = contigLength - end;
            endOnStrand = contigLength - begin;
        }
        return newBuilder()
                .setContig(contig)
                .setBegin(beginOnStrand)
                .setEnd(endOnStrand)
                .setStrand(strand)
                .setContigLength(contigLength)
                .build();
    }

    public static final class Builder {

        private String contig;

        private int begin;

        private int end;

        private boolean strand;

        private int contigLength;

        private Builder() {
        }

        public Builder setContig(String contig) {
            this.contig = contig;
            return this;
        }

        public Builder setBegin(int begin) {
            this.begin = begin;
            return this;
        }

        public Builder setEnd(int end) {
            this.end = end;
            return this;
        }

        public Builder setStrand(boolean strand) {
            this.strand = strand;
            return this;
        }

        public Builder setContigLength(int contigLength) {
            this.contigLength = contigLength;
            return this;
        }

        public GenomeInterval build() {
            return new GenomeInterval(this);
        }
    }
}
