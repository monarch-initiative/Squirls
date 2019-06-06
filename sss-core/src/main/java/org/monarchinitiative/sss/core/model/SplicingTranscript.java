package org.monarchinitiative.sss.core.model;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public class SplicingTranscript {

    private static final SplicingTranscript DEFAULT = SplicingTranscript.newBuilder().build();

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


    public static SplicingTranscript getDefaultInstance() {
        return DEFAULT;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public ImmutableList<SplicingExon> getExons() {
        return exons;
    }

    public ImmutableList<SplicingIntron> getIntrons() {
        return introns;
    }

    public String getAccessionId() {
        return accessionId;
    }

    public boolean getStrand() {
        return interval.isStrand();
    }

    public String getContig() {
        return interval.getContig();
    }

    public int getTxBegin() {
        return interval.getBegin();
    }

    public int getTxBeginOnFwd() {
        if (interval.isStrand()) { // forward strand
            return getTxBegin();
        } else {
            return interval.getContigLength() - interval.getEnd();
        }
    }

    public int getTxEnd() {
        return interval.getEnd();
    }

    public int getTxEndOnFwd() {
        if (interval.isStrand()) {
            return getTxEnd();
        } else {
            return interval.getContigLength() - interval.getBegin();
        }
    }

    public static final class Builder {

        private GenomeInterval interval = GenomeInterval.getDefaultInstance();

        private List<SplicingExon> exons = new ArrayList<>();

        private List<SplicingIntron> introns = new ArrayList<>();

        private String accessionId = "";

        private Builder() {
            // private no-op
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
