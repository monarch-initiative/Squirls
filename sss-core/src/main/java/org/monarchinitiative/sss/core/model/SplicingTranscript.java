package org.monarchinitiative.sss.core.model;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public class SplicingTranscript {

    private final GenomeInterval interval;

    private final String accessionId;

    private final ImmutableList<SplicingExon> exons;

    private final ImmutableList<SplicingIntron> introns;

    private SplicingTranscript(Builder builder) {
        interval = builder.interval;
        exons = ImmutableList.copyOf(builder.exons);
        introns = ImmutableList.copyOf(builder.introns);
        accessionId = builder.accessionId;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getAccessionId() {
        return accessionId;
    }

    public boolean getStrand() {
        return interval.isStrand();
    }

    public int getTxBegin() {
        return interval.getBegin();
    }

    public int getTxEnd() {
        return interval.getEnd();
    }

    public static final class Builder {

        private GenomeInterval interval;

        private List<SplicingExon> exons;

        private List<SplicingIntron> introns;

        private String accessionId;

        private Builder() {
            exons = new ArrayList<>();
            introns = new ArrayList<>();
        }

        public Builder setAccessionId(String accessionId) {
            this.accessionId = accessionId;
            return this;
        }

        public Builder setInterval(GenomeInterval interval) {
            this.interval = interval;
            return this;
        }

        public Builder addExon(SplicingExon exon) {
            this.exons.add(exon);
            return this;
        }

        public Builder addAllExons(Collection<SplicingExon> exons) {
            this.exons.addAll(exons);
            return this;
        }

        public Builder addIntron(SplicingIntron intron) {
            this.introns.add(intron);
            return this;
        }

        public Builder addAllIntrons(Collection<SplicingIntron> introns) {
            this.introns.addAll(introns);
            return this;
        }

        public SplicingTranscript build() {
            return new SplicingTranscript(this);
        }
    }
}
