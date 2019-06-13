package org.monarchinitiative.threes.core.model;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public class SplicingTranscript {

    private static final SplicingTranscript DEFAULT = SplicingTranscript.newBuilder().build();

    private final GenomeCoordinates txRegionCoordinates;

    private final String accessionId;

    private final ImmutableList<SplicingExon> exons;

    private final ImmutableList<SplicingIntron> introns;

    private SplicingTranscript(Builder builder) {
        txRegionCoordinates = builder.coordinates;
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

    public GenomeCoordinates getTxRegionCoordinates() {
        return txRegionCoordinates;
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
        return txRegionCoordinates.isStrand();
    }

    public String getContig() {
        return txRegionCoordinates.getContig();
    }

    public int getTxBegin() {
        return txRegionCoordinates.getBegin();
    }


    public int getTxEnd() {
        return txRegionCoordinates.getEnd();
    }

    public int getTxLength() {
        return txRegionCoordinates.getLength();
    }

    @Override
    public String toString() {
        return "SplicingTranscript{" +
                "txRegionCoordinates=" + txRegionCoordinates +
                ", accessionId='" + accessionId + '\'' +
                ", exons=" + exons +
                ", introns=" + introns +
                '}';
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
