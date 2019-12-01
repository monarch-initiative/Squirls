package org.monarchinitiative.threes.core.model;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;

/**
 * Container for tunable parameters for scoring of splicing variants.
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 */
public class SplicingParameters {

    /**
     * N coding (exonic) nts that form the canonical splice donor site.
     */
    private final int donorExonic;

    /**
     * N non-coding/intronic nts that form the canonical splice donor site.
     */
    private final int donorIntronic;

    /**
     * N intronic nts that form the canonical splice acceptor site.
     */
    private final int acceptorIntronic;

    /**
     * N coding (exonic) nts that form the canonical splice acceptor site.
     */
    private final int acceptorExonic;


    private SplicingParameters(Builder builder) {
        this.donorExonic = builder.donorExonic;
        this.donorIntronic = builder.donorIntronic;
        this.acceptorIntronic = builder.acceptorIntronic;
        this.acceptorExonic = builder.acceptorExonic;
    }


    public static Builder builder() {
        return new Builder();
    }


    public int getDonorExonic() {
        return donorExonic;
    }


    public int getDonorIntronic() {
        return donorIntronic;
    }


    public int getDonorLength() {
        return donorExonic + donorIntronic;
    }


    public int getAcceptorExonic() {
        return acceptorExonic;
    }


    public int getAcceptorIntronic() {
        return acceptorIntronic;
    }


    public int getAcceptorLength() {
        return acceptorExonic + acceptorIntronic;
    }

    /**
     * @param anchor {@link GenomePosition} representing `exon|intron` boundary
     * @return {@link GenomeInterval} representing splice donor site
     */
    public GenomeInterval makeDonorRegion(GenomePosition anchor) {
        return new GenomeInterval(anchor.shifted(-donorExonic), donorExonic + donorIntronic);
    }

    /**
     * @param anchor {@link GenomePosition} representing `intron|exon` boundary
     * @return {@link GenomeInterval} representing splice acceptor site
     */
    public GenomeInterval makeAcceptorRegion(GenomePosition anchor) {
        return new GenomeInterval(anchor.shifted(-acceptorIntronic), acceptorIntronic + acceptorExonic);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SplicingParameters)) return false;

        SplicingParameters that = (SplicingParameters) o;

        if (donorExonic != that.donorExonic) return false;
        if (donorIntronic != that.donorIntronic) return false;
        if (acceptorIntronic != that.acceptorIntronic) return false;
        return acceptorExonic == that.acceptorExonic;

    }

    @Override
    public int hashCode() {
        int result = donorExonic;
        result = 31 * result + donorIntronic;
        result = 31 * result + acceptorIntronic;
        result = 31 * result + acceptorExonic;
        return result;
    }

    @Override
    public String toString() {
        return "SplicingParameters{" +
                "donorExonic=" + donorExonic +
                ", donorIntronic=" + donorIntronic +
                ", acceptorIntronic=" + acceptorIntronic +
                ", acceptorExonic=" + acceptorExonic +
                '}';
    }

    public static final class Builder {

        private int donorExonic = 3;

        private int donorIntronic = 6;

        private int acceptorIntronic = 25;

        private int acceptorExonic = 2;


        private Builder() {
        }


        public Builder setDonorExonic(int donorExonic) {
            this.donorExonic = donorExonic;
            return this;
        }


        public Builder setDonorIntronic(int donorIntronic) {
            this.donorIntronic = donorIntronic;
            return this;
        }


        public Builder setAcceptorIntronic(int acceptorIntronic) {
            this.acceptorIntronic = acceptorIntronic;
            return this;
        }


        public Builder setAcceptorExonic(int acceptorExonic) {
            this.acceptorExonic = acceptorExonic;
            return this;
        }

        public SplicingParameters build() {
            return new SplicingParameters(this);
        }
    }

}
