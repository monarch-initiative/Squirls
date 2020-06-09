package org.monarchinitiative.squirls.core;

import de.charite.compbio.jannovar.reference.GenomeInterval;
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
     * Coordinates of the donor sites closest to given variant.
     */
    private final Map<String, GenomeInterval> donorCoordinateMap;
    /**
     * Coordinates of the donor sites closest to given variant.
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
        return Objects.equals(donorCoordinateMap, metadata.donorCoordinateMap) &&
                Objects.equals(acceptorCoordinateMap, metadata.acceptorCoordinateMap) &&
                Objects.equals(meanPhyloPConservation, metadata.meanPhyloPConservation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(donorCoordinateMap, acceptorCoordinateMap, meanPhyloPConservation);
    }

    public static final class Builder {
        private final Map<String, GenomeInterval> donorCoordinateMap = new HashMap<>();
        private final Map<String, GenomeInterval> acceptorCoordinateMap = new HashMap<>();
        private Double meanPhyloPScore = Double.NaN;

        private Builder() {
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
