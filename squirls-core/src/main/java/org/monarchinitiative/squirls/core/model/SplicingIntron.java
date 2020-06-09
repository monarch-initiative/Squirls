package org.monarchinitiative.squirls.core.model;

import de.charite.compbio.jannovar.reference.GenomeInterval;

import java.util.Objects;

/**
 *
 */
public class SplicingIntron extends SplicingRegion {

    private final double donorScore;

    private final double acceptorScore;

    private SplicingIntron(Builder builder) {
        super(builder.interval);
        donorScore = builder.donorScore;
        acceptorScore = builder.acceptorScore;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SplicingIntron intron = (SplicingIntron) o;
        return Double.compare(intron.donorScore, donorScore) == 0 &&
                Double.compare(intron.acceptorScore, acceptorScore) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), donorScore, acceptorScore);
    }

    @Override
    public String toString() {
        return "SplicingIntron{" +
                interval +
                ", donor=" + donorScore +
                ", acceptor=" + acceptorScore +
                "}";
    }

    public double getDonorScore() {
        return donorScore;
    }

    public double getAcceptorScore() {
        return acceptorScore;
    }

    public static final class Builder {

        private GenomeInterval interval;

        private double donorScore;

        private double acceptorScore;

        private Builder() {
        }

        public Builder setInterval(GenomeInterval interval) {
            this.interval = interval;
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
