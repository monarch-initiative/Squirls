package org.monarchinitiative.squirls.core.classifier;

import java.util.Objects;

/**
 * This class represents a fragment of information from the decision function, a single prediction of an ensemble
 * which calculated pathogenicity probability.
 */
public class PartialPrediction {

    private final String name;
    private final double pathoProba;
    private final double threshold;

    private PartialPrediction(String name, double pathoProba, double threshold) {
        this.name = name;
        this.pathoProba = pathoProba;
        this.threshold = threshold;
    }

    public static PartialPrediction of(String name, double pathoProba, double threshold) {
        return new PartialPrediction(name, pathoProba, threshold);
    }

    public String getName() {
        return name;
    }

    public double getThreshold() {
        return threshold;
    }

    public double getPathoProba() {
        return pathoProba;
    }

    public boolean isPathogenic() {
        return pathoProba > threshold;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartialPrediction that = (PartialPrediction) o;
        return Double.compare(that.pathoProba, pathoProba) == 0 &&
                Double.compare(that.threshold, threshold) == 0 &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, pathoProba, threshold);
    }

    @Override
    public String toString() {
        return "PartialPrediction{" +
                "name='" + name + '\'' +
                ", pathoProba=" + pathoProba +
                ", threshold=" + threshold +
                '}';
    }
}
