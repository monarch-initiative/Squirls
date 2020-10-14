package org.monarchinitiative.squirls.core.classifier.io;

import java.util.List;
import java.util.Objects;

/**
 * Parameters for logistic regression for performing prediction transformation.
 */
public class PredictionTransformationParameters {

    private final List<List<Double>> slope;
    private final List<Double> intercept;

    public PredictionTransformationParameters(List<List<Double>> slope, List<Double> intercept) {
        this.slope = slope;
        this.intercept = intercept;
        check();
    }

    private void check() {
        // SLOPE
        // 0 - at least one row
        if (slope.isEmpty()) {
            throw new IllegalArgumentException("Slope parameters cannot be empty");
        }

        // 1 -all rows have the same number of columns
        int previous = -1;
        for (int i = 0; i < slope.size(); i++) {
            final List<Double> current = slope.get(i);
            if (previous < 0) {
                // happens in the first loop
                previous = current.size();
            }
            if (current.size() != previous) {
                throw new IllegalArgumentException(String.format("Row %d: %d columns, row %d: %d columns", i, current.size(), i - 1, previous));
            }
        }

        // INTERCEPT
        if (intercept.isEmpty()) {
            throw new IllegalArgumentException("Intercept cannot be empty");
        }
    }

    public List<List<Double>> getSlope() {
        return slope;
    }

    public List<Double> getIntercept() {
        return intercept;
    }

    public double getDonorSlope() {
        return slope.get(0).get(0);
    }

    public double getAcceptorSlope() {
        return slope.get(0).get(1);
    }

    public double getInterceptScalar() {
        return intercept.get(0);
    }

    @Override
    public String toString() {
        return "LogisticRegressionParameters{" +
                "slope=" + slope +
                ", intercept=" + intercept +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PredictionTransformationParameters that = (PredictionTransformationParameters) o;
        return Objects.equals(slope, that.slope) &&
                Objects.equals(intercept, that.intercept);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slope, intercept);
    }
}
