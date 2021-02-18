package org.monarchinitiative.squirls.io.classifier.v046;

import java.util.List;
import java.util.Objects;

/**
 * @author Daniel Danis
 */
public class ScalingTransferModelV046 {

    private List<Double> standardScalerMean;
    private List<Double> standardScalerVar;
    private List<List<Double>> logisticRegressionCoef;
    private List<Double> logisticRegressionIntercept;

    public List<Double> getStandardScalerMean() {
        return standardScalerMean;
    }

    public void setStandardScalerMean(List<Double> standardScalerMean) {
        this.standardScalerMean = standardScalerMean;
    }

    public List<Double> getStandardScalerVar() {
        return standardScalerVar;
    }

    public void setStandardScalerVar(List<Double> standardScalerVar) {
        this.standardScalerVar = standardScalerVar;
    }

    public List<List<Double>> getLogisticRegressionCoef() {
        return logisticRegressionCoef;
    }

    public void setLogisticRegressionCoef(List<List<Double>> logisticRegressionCoef) {
        this.logisticRegressionCoef = logisticRegressionCoef;
    }

    public List<Double> getLogisticRegressionIntercept() {
        return logisticRegressionIntercept;
    }

    public void setLogisticRegressionIntercept(List<Double> logisticRegressionIntercept) {
        this.logisticRegressionIntercept = logisticRegressionIntercept;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScalingTransferModelV046 that = (ScalingTransferModelV046) o;
        return Objects.equals(standardScalerMean, that.standardScalerMean) && Objects.equals(standardScalerVar, that.standardScalerVar) && Objects.equals(logisticRegressionCoef, that.logisticRegressionCoef) && Objects.equals(logisticRegressionIntercept, that.logisticRegressionIntercept);
    }

    @Override
    public int hashCode() {
        return Objects.hash(standardScalerMean, standardScalerVar, logisticRegressionCoef, logisticRegressionIntercept);
    }

    @Override
    public String toString() {
        return "ScalingTransferModel{" +
                "standardScalerMean=" + standardScalerMean +
                ", standardScalerVar=" + standardScalerVar +
                ", logisticRegressionCoef=" + logisticRegressionCoef +
                ", logisticRegressionIntercept=" + logisticRegressionIntercept +
                '}';
    }
}
