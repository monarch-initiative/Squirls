package org.monarchinitiative.sss.core.model;

import org.monarchinitiative.sss.core.reference.InvalidCoordinatesException;

/**
 *
 */
public class GenomeCoordinates {

    private final String contig;

    private final int begin;

    private final int end;

    private final boolean strand;

    private GenomeCoordinates(Builder builder) throws InvalidCoordinatesException {
        if (builder.contig == null || builder.contig.isEmpty()) {
            throw new InvalidCoordinatesException("Contig is missing");
        } else {
            contig = builder.contig;
        }

        if (builder.begin < 0 || builder.end < 0) {
            throw new InvalidCoordinatesException("Coordinate is negative");
        }

        if (builder.begin > builder.end) {
            throw new InvalidCoordinatesException("Begin is larger than end");
        } else {
            begin = builder.begin;
            end = builder.end;
        }

        strand = builder.strand;
    }


    public static Builder newBuilder() {
        return new Builder();
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

    public boolean isStrand() {
        return strand;
    }

    public int getLength() {
        return end - begin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GenomeCoordinates)) return false;

        GenomeCoordinates that = (GenomeCoordinates) o;

        if (begin != that.begin) return false;
        if (end != that.end) return false;
        if (strand != that.strand) return false;
        return contig != null ? contig.equals(that.contig) : that.contig == null;

    }

    @Override
    public int hashCode() {
        int result = contig != null ? contig.hashCode() : 0;
        result = 31 * result + begin;
        result = 31 * result + end;
        result = 31 * result + (strand ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "coor{" + contig + ':' +
                "[" + begin +
                "-" + end +
                "](" + strand +
                ')';
    }

    public static final class Builder {

        private String contig;

        private int begin;

        private int end;

        private boolean strand;

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

        public GenomeCoordinates build() throws InvalidCoordinatesException {
            return new GenomeCoordinates(this);
        }
    }
}
