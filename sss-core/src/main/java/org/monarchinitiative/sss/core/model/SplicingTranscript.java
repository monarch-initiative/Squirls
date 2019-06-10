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

    private final GenomeCoordinates coordinates;

    private final String accessionId;

    private final ImmutableList<SplicingExon> exons;

    private final ImmutableList<SplicingIntron> introns;

    private SplicingTranscript(Builder builder) {
        coordinates = builder.coordinates;
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

    public GenomeCoordinates getCoordinates() {
        return coordinates;
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
        return coordinates.isStrand();
    }

    public String getContig() {
        return coordinates.getContig();
    }

    public int getTxBegin() {
        return coordinates.getBegin();
    }


    public int getTxEnd() {
        return coordinates.getEnd();
    }


    public static final class Builder {

        private GenomeCoordinates coordinates;

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

        public Builder setCoordinates(GenomeCoordinates coordinates) {
            this.coordinates = coordinates;
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
