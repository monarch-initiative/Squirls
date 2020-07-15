package org.monarchinitiative.squirls.core;

import de.charite.compbio.jannovar.reference.GenomePosition;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// TODO: 8. 6. 2020 - revise docs

/**
 * This class is a kitchen sink for all data we need to make a nice figures or anything else downstream.
 * <p>
 * Each instance contains information with respect to a single
 * {@link de.charite.compbio.jannovar.reference.GenomeVariant} and a single
 * {@link SplicingTranscript}.
 * <p>
 * Therefore, it is necessary for it to reside within {@link SplicingPredictionData} instance which contains these
 * information.
 */
public class Metadata {

    /**
     * A singleton empty instance.
     */
    private static final Metadata EMPTY = new Metadata();

    /**
     * Map with transcript accession ID to coordinates of the donor site closest to the variant.
     * <p>
     * The coordinate represents the 1-based position of the first intronic base. In 0-based coordinate system,
     * the coordinate represents the exon|intron boundary.
     */
    private final Map<String, GenomePosition> donorCoordinateMap;

    /**
     * Map with transcript accession ID to coordinates of the acceptor site closest to the variant.
     * <p>
     * The coordinate represents the 1-based position of the first exonic base. In 0-based coordinate system,
     * the coordinate represents the intron|exon boundary.
     */
    private final Map<String, GenomePosition> acceptorCoordinateMap;

    /**
     * Mean PhyloP score of the region spanned by the <em>REF</em> variant allele.
     */
    private final Double meanPhyloPConservation;

    /**
     * Special private constructor for creating {@link #EMPTY} singleton instance.
     */
    private Metadata() {
        donorCoordinateMap = Map.of();
        acceptorCoordinateMap = Map.of();
        meanPhyloPConservation = Double.NaN;
    }

    private Metadata(Builder builder) {
        donorCoordinateMap = Map.copyOf(builder.donorCoordinateMap);
        acceptorCoordinateMap = Map.copyOf(builder.acceptorCoordinateMap);
        meanPhyloPConservation = builder.meanPhyloPScore;
    }

    public static Metadata empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }


    public Double getMeanPhyloPConservation() {
        return meanPhyloPConservation;
    }

    public Map<String, GenomePosition> getDonorCoordinateMap() {
        return donorCoordinateMap;
    }

    public Map<String, GenomePosition> getAcceptorCoordinateMap() {
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
        return Objects.equals(donorCoordinateMap, metadata.donorCoordinateMap) &&
                Objects.equals(acceptorCoordinateMap, metadata.acceptorCoordinateMap) &&
                Objects.equals(meanPhyloPConservation, metadata.meanPhyloPConservation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(donorCoordinateMap, acceptorCoordinateMap, meanPhyloPConservation);
    }

    public static final class Builder {
        private final Map<String, GenomePosition> donorCoordinateMap = new HashMap<>();
        private final Map<String, GenomePosition> acceptorCoordinateMap = new HashMap<>();
        private Double meanPhyloPScore = Double.NaN;

        private Builder() {
        }


        public Builder putDonorCoordinate(String txAccession, GenomePosition donorPosition) {
            this.donorCoordinateMap.put(txAccession, donorPosition);
            return this;
        }

        public Builder putAllDonorCoordinates(Map<String, GenomePosition> donorCoordinateMap) {
            this.donorCoordinateMap.putAll(donorCoordinateMap);
            return this;
        }

        public Builder putAcceptorCoordinate(String txAccession, GenomePosition acceptorPosition) {
            this.acceptorCoordinateMap.put(txAccession, acceptorPosition);
            return this;
        }

        public Builder putAllAcceptorCoordinates(Map<String, GenomePosition> acceptorCoordinateMap) {
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
