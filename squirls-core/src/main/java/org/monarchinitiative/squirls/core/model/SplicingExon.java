package org.monarchinitiative.squirls.core.model;

import de.charite.compbio.jannovar.reference.GenomeInterval;

/**
 * Wrapper for exon interval with space for possible future exon attributes.
 */
public class SplicingExon extends SplicingRegion {


    private SplicingExon(Builder builder) {
        super(builder.interval);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "SplicingExon{" +
                interval +
                "} ";
    }

    public static final class Builder {

        private GenomeInterval interval;


        private Builder() {
        }

        public Builder setInterval(GenomeInterval interval) {
            this.interval = interval;
            return this;
        }


        public SplicingExon build() {
            return new SplicingExon(this);
        }
    }
}
