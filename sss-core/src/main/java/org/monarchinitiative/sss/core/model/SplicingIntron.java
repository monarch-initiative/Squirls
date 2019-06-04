package org.monarchinitiative.sss.core.model;

/**
 *
 */
public class SplicingIntron {

    private final int begin;

    private final int end;

    private final double donorScore;

    private final double acceptorScore;

    private SplicingIntron(Builder builder) {
        begin = builder.begin;
        end = builder.end;
        donorScore = builder.donorScore;
        acceptorScore = builder.acceptorScore;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SplicingIntron)) return false;

        SplicingIntron that = (SplicingIntron) o;

        if (begin != that.begin) return false;
        if (end != that.end) return false;
        if (Double.compare(that.donorScore, donorScore) != 0) return false;
        return Double.compare(that.acceptorScore, acceptorScore) == 0;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = begin;
        result = 31 * result + end;
        temp = Double.doubleToLongBits(donorScore);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(acceptorScore);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "SplicingIntron{" +
                "begin=" + begin +
                ", end=" + end +
                ", donorScore=" + donorScore +
                ", acceptorScore=" + acceptorScore +
                '}';
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }

    public double getDonorScore() {
        return donorScore;
    }

    public double getAcceptorScore() {
        return acceptorScore;
    }

    public static final class Builder {

        private int begin;

        private int end;

        private double donorScore;

        private double acceptorScore;

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

        public Builder setDonorScore(double donorScore) {
            this.donorScore = donorScore;
            return this;
        }

        public Builder setAcceptorScore(double acceptorScore) {
            this.acceptorScore = acceptorScore;
            return this;
        }

        public SplicingIntron build() {
            return new SplicingIntron(this);
        }
    }
}
