package org.monarchinitiative.threes.core;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Metadata {

    private static final Metadata EMPTY = new Metadata();

    /**
     * The variant in question.
     */
    private final GenomeVariant variant;
    /**
     * FASTA sequence long enough to span all the {@link GenomeInterval}s present in this instance.
     */
    private final SequenceInterval sequence;
    /**
     * Coordinates of the donor sites closest to given {@link #variant}.
     */
    private final Map<String, GenomeInterval> donorCoordinateMap;
    /**
     * Coordinates of the donor sites closest to given {@link #variant}.
     */
    private final Map<String, GenomeInterval> acceptorCoordinateMap;
    /**
     * Mean PhyloP score of the region spanned by the <em>REF</em> variant allele.
     */
    private final Double meanPhyloPConservation;

    /**
     * Special private constructor for creating {@link #EMPTY} singleton instance.
     */
    private Metadata() {
        variant = null;
        sequence = null;
        donorCoordinateMap = Map.of();
        acceptorCoordinateMap = Map.of();
        meanPhyloPConservation = Double.NaN;
    }

    private Metadata(Builder builder) {
        variant = Objects.requireNonNull(builder.variant);
        donorCoordinateMap = Map.copyOf(builder.donorCoordinateMap);
        acceptorCoordinateMap = Map.copyOf(builder.acceptorCoordinateMap);
        meanPhyloPConservation = builder.meanPhyloPScore;

        sequence = trimSequence(builder.sequence);
    }

    public static Metadata empty() {
        return EMPTY;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private SequenceInterval trimSequence(SequenceInterval sequence) {
        // TODO: 4. 6. 2020 trim the sequence interval to only contain the sequence for the present data
        return sequence;
    }

    public GenomeVariant getVariant() {
        return variant;
    }

    public SequenceInterval getSequence() {
        return sequence;
    }

    public Double getMeanPhyloPConservation() {
        return meanPhyloPConservation;
    }

    public Map<String, GenomeInterval> getDonorCoordinateMap() {
        return donorCoordinateMap;
    }

    public Map<String, GenomeInterval> getAcceptorCoordinateMap() {
        return acceptorCoordinateMap;
    }

    /**
     * @return <code>true</code> if the metadata instance is equal to the empty/singleton metadata instance
     */
    public boolean isEmpty() {
        return equals(EMPTY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metadata metadata = (Metadata) o;
        return Objects.equals(variant, metadata.variant) &&
                Objects.equals(sequence, metadata.sequence) &&
                Objects.equals(donorCoordinateMap, metadata.donorCoordinateMap) &&
                Objects.equals(acceptorCoordinateMap, metadata.acceptorCoordinateMap) &&
                Objects.equals(meanPhyloPConservation, metadata.meanPhyloPConservation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variant, sequence, donorCoordinateMap, acceptorCoordinateMap, meanPhyloPConservation);
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "variant=" + variant +
                ", sequence=" + sequence +
                ", donorCoordinateMap=" + donorCoordinateMap +
                ", acceptorCoordinateMap=" + acceptorCoordinateMap +
                ", meanPhyloPConservation=" + meanPhyloPConservation +
                '}';
    }

    public static final class Builder {
        private final Map<String, GenomeInterval> donorCoordinateMap = new HashMap<>();
        private final Map<String, GenomeInterval> acceptorCoordinateMap = new HashMap<>();
        private Double meanPhyloPScore = Double.NaN;
        private GenomeVariant variant;
        private SequenceInterval sequence;

        private Builder() {
        }

        public Builder variant(GenomeVariant variant) {
            this.variant = variant;
            return this;
        }

        public Builder sequence(SequenceInterval sequence) {
            this.sequence = sequence;
            return this;
        }

        public Builder putDonorCoordinate(String txAccession, GenomeInterval donorInterval) {
            this.donorCoordinateMap.put(txAccession, donorInterval);
            return this;
        }

        public Builder putAllDonorCoordinates(Map<String, GenomeInterval> donorCoordinateMap) {
            this.donorCoordinateMap.putAll(donorCoordinateMap);
            return this;
        }

        public Builder putAcceptorCoordinate(String txAccession, GenomeInterval acceptorInterval) {
            this.acceptorCoordinateMap.put(txAccession, acceptorInterval);
            return this;
        }

        public Builder putAllAcceptorCoordinates(Map<String, GenomeInterval> acceptorCoordinateMap) {
            this.acceptorCoordinateMap.putAll(acceptorCoordinateMap);
            return this;
        }

        public Builder meanPhyloPScore(double score) {
            this.meanPhyloPScore = score;
            return this;
        }

        public Metadata build() {
            return new Metadata(this);
        }
    }
}
