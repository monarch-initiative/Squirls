package org.monarchinitiative.squirls.core.model;

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * POJO for transcript data used within 3S codebase.
 */
public class SplicingTranscript {

    public static final String EXON_REGION_CODE = "ex";
    public static final String INTRON_REGION_CODE = "ir";

    private static final SplicingTranscript DEFAULT = SplicingTranscript.builder().build();

    private final GenomeInterval txRegionCoordinates;

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

    public static Builder builder() {
        return new Builder();
    }

    public GenomeInterval getTxRegionCoordinates() {
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

    public Strand getStrand() {
        return txRegionCoordinates.getStrand();
    }

    public int getChr() {
        return txRegionCoordinates.getChr();
    }

    public String getChrName() {
        return txRegionCoordinates.getRefDict().getContigIDToName().get(txRegionCoordinates.getChr());
    }

    public int getTxBegin() {
        return txRegionCoordinates.getBeginPos();
    }


    public int getTxEnd() {
        return txRegionCoordinates.getEndPos();
    }

    public int getTxLength() {
        return txRegionCoordinates.length();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SplicingTranscript that = (SplicingTranscript) o;
        return Objects.equals(txRegionCoordinates, that.txRegionCoordinates) &&
                Objects.equals(accessionId, that.accessionId) &&
                Objects.equals(exons, that.exons) &&
                Objects.equals(introns, that.introns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(txRegionCoordinates, accessionId, exons, introns);
    }

    public static final class Builder {

        private final List<SplicingExon> exons = new ArrayList<>();
        private final List<SplicingIntron> introns = new ArrayList<>();
        private GenomeInterval coordinates;
        private String accessionId = "";

        private Builder() {
            // private no-op
        }

        public Builder setAccessionId(String accessionId) {
            this.accessionId = accessionId;
            return this;
        }

        public Builder setCoordinates(GenomeInterval coordinates) {
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
