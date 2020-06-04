package org.monarchinitiative.threes.core.scoring;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import org.monarchinitiative.threes.core.classifier.FeatureData;

import java.util.Objects;
import java.util.Optional;

public class SplicingAnnotationData {

    private final FeatureData featureData;
    private final GenomeInterval donorCoordinates;
    private final GenomeInterval acceptorCoordinates;
    private final Double meanPhyloPScore;

    private SplicingAnnotationData(Builder builder) {
        featureData = builder.featureData;
        donorCoordinates = builder.donorCoordinates;
        acceptorCoordinates = builder.acceptorCoordinates;
        meanPhyloPScore = builder.meanPhyloPScore;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Double getMeanPhyloPScore() {
        return meanPhyloPScore;
    }

    public Optional<GenomeInterval> getDonorCoordinates() {
        return Optional.ofNullable(donorCoordinates);
    }

    public Optional<GenomeInterval> getAcceptorCoordinates() {
        return Optional.ofNullable(acceptorCoordinates);
    }

    public FeatureData getFeatureData() {
        return featureData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SplicingAnnotationData that = (SplicingAnnotationData) o;
        return Objects.equals(featureData, that.featureData) &&
                Objects.equals(donorCoordinates, that.donorCoordinates) &&
                Objects.equals(acceptorCoordinates, that.acceptorCoordinates) &&
                Objects.equals(meanPhyloPScore, that.meanPhyloPScore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(featureData, donorCoordinates, acceptorCoordinates, meanPhyloPScore);
    }

    @Override
    public String toString() {
        return "SplicingAnnotationData{" +
                "featureData=" + featureData +
                ", donorCoordinates=" + donorCoordinates +
                ", acceptorCoordinates=" + acceptorCoordinates +
                ", meanPhyloPScore=" + meanPhyloPScore +
                '}';
    }

    public static final class Builder {
        private GenomeInterval donorCoordinates = null;
        private GenomeInterval acceptorCoordinates = null;
        private FeatureData featureData;
        private Double meanPhyloPScore = Double.NaN;

        private Builder() {
        }

        public Builder featureData(FeatureData featureData) {
            this.featureData = featureData;
            return this;
        }

        public Builder putDonorCoordinates(GenomeInterval donorInterval) {
            this.donorCoordinates = donorInterval;
            return this;
        }

        public Builder putAcceptorCoordinates(GenomeInterval acceptorInterval) {
            this.acceptorCoordinates = acceptorInterval;
            return this;
        }

        public Builder meanPhyloPScore(double meanPhyloPScore) {
            this.meanPhyloPScore = meanPhyloPScore;
            return this;
        }

        public SplicingAnnotationData build() {
            return new SplicingAnnotationData(this);
        }
    }

}
