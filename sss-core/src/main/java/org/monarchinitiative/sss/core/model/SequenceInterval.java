package org.monarchinitiative.sss.core.model;

/**
 * POJO for grouping together a nucleotide sequence and coordinate system.
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 */
public class SequenceInterval {

    private final String contig;

    private final int begin, end;

    private final String sequence;

    private final boolean strand;

    private final int contigLength;

    private SequenceInterval(Builder builder) {
        contig = builder.contig;
        begin = builder.begin;
        end = builder.end;
        sequence = builder.sequence;
        strand = builder.strand;
        contigLength = builder.contigLength;

        // TODO - add sanity checks
    }

    /**
     * Convert nucleotide sequence to reverse complement.
     *
     * @param sequence of nucleotides, only {a,c,g,t,n,A,C,G,T,N} permitted
     * @return reverse complement of given <code>sequence</code>
     * @throws IllegalArgumentException if there is an unpermitted character present
     */
    public static String reverseComplement(String sequence) {
        char[] oldSeq = sequence.toCharArray();
        char[] newSeq = new char[oldSeq.length];
        int idx = oldSeq.length - 1;
        for (int i = 0; i < oldSeq.length; i++) {
            if (oldSeq[i] == 'A') {
                newSeq[idx - i] = 'T';
            } else if (oldSeq[i] == 'a') {
                newSeq[idx - i] = 't';
            } else if (oldSeq[i] == 'T') {
                newSeq[idx - i] = 'A';
            } else if (oldSeq[i] == 't') {
                newSeq[idx - i] = 'a';
            } else if (oldSeq[i] == 'C') {
                newSeq[idx - i] = 'G';
            } else if (oldSeq[i] == 'c') {
                newSeq[idx - i] = 'g';
            } else if (oldSeq[i] == 'G') {
                newSeq[idx - i] = 'C';
            } else if (oldSeq[i] == 'g') {
                newSeq[idx - i] = 'c';
            } else if (oldSeq[i] == 'N') {
                newSeq[idx - i] = 'N';
            } else if (oldSeq[i] == 'n') {
                newSeq[idx - i] = 'n';
            } else
                throw new IllegalArgumentException(String.format("Illegal nucleotide %s in sequence %s",
                        oldSeq[i], sequence));
        }
        return new String(newSeq);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(String contig, int begin, int end, String sequence, boolean strand, int contigLength) {
        return new Builder().setContig(contig).setBegin(begin).setEnd(end).setSequence(sequence).setStrand(strand).setContigLength(contigLength);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SequenceInterval)) return false;

        SequenceInterval that = (SequenceInterval) o;

        if (begin != that.begin) return false;
        if (end != that.end) return false;
        if (strand != that.strand) return false;
        if (contigLength != that.contigLength) return false;
        if (contig != null ? !contig.equals(that.contig) : that.contig != null) return false;
        return sequence != null ? sequence.equals(that.sequence) : that.sequence == null;

    }

    @Override
    public int hashCode() {
        int result = contig != null ? contig.hashCode() : 0;
        result = 31 * result + begin;
        result = 31 * result + end;
        result = 31 * result + (sequence != null ? sequence.hashCode() : 0);
        result = 31 * result + (strand ? 1 : 0);
        result = 31 * result + contigLength;
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s:[%d-%d](%d)%s %s", contig, begin, end, contigLength, strand ? "+" : "-", sequence);
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

    public String getSequence() {
        return sequence;
    }


    public SequenceInterval withStrand(boolean strand) {
        final int beginOnStrand, endOnStrand;
        final String sequenceOnStrand;
        if (strand == this.strand) {
            beginOnStrand = begin;
            endOnStrand = end;
            sequenceOnStrand = sequence;
        } else {
            beginOnStrand = contigLength - end;
            endOnStrand = contigLength - begin;
            sequenceOnStrand = reverseComplement(sequence);
        }
        return newBuilder()
                .setContig(contig)
                .setBegin(beginOnStrand)
                .setEnd(endOnStrand)
                .setStrand(strand)
                .setSequence(sequenceOnStrand)
                .setContigLength(contigLength)
                .build();
    }


    /**
     * {@code SequenceInterval} builder static inner class.
     */
    public static final class Builder {

        private String contig;

        private int begin;

        private int end;

        private String sequence;

        private boolean strand;

        private int contigLength;

        private Builder() {
        }

        /**
         * Sets the {@code contig} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param contig the {@code contig} to set
         * @return a reference to this Builder
         */
        public Builder setContig(String contig) {
            this.contig = contig;
            return this;
        }

        /**
         * Sets the {@code begin} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param begin the {@code begin} to set
         * @return a reference to this Builder
         */
        public Builder setBegin(int begin) {
            this.begin = begin;
            return this;
        }

        /**
         * Sets the {@code end} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param end the {@code end} to set
         * @return a reference to this Builder
         */
        public Builder setEnd(int end) {
            this.end = end;
            return this;
        }

        /**
         * Sets the {@code sequence} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param sequence the {@code sequence} to set
         * @return a reference to this Builder
         */
        public Builder setSequence(String sequence) {
            this.sequence = sequence;
            return this;
        }

        /**
         * Sets the {@code strand} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param strand the {@code strand} to set, <code>true</code> if FWD, <code>false</code> if REV
         * @return a reference to this Builder
         */
        public Builder setStrand(boolean strand) {
            this.strand = strand;
            return this;
        }

        /**
         * Sets the {@code contigLength} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param contigLength the {@code contigLength} to set
         * @return a reference to this Builder
         */
        public Builder setContigLength(int contigLength) {
            this.contigLength = contigLength;
            return this;
        }

        /**
         * Returns a {@code SequenceInterval} built from the parameters previously set.
         *
         * @return a {@code SequenceInterval} built with parameters of this {@code SequenceInterval.Builder}
         */
        public SequenceInterval build() {
            return new SequenceInterval(this);
        }
    }
}
