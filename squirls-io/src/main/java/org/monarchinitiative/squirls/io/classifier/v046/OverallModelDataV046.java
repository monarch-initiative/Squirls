package org.monarchinitiative.squirls.io.classifier.v046;

import org.monarchinitiative.squirls.io.classifier.reference.PipelineTransferModel;

import java.util.List;
import java.util.Objects;

/**
 * POJO for representing the content that we expect to find in a well-formatted YAML file.
 * @author Daniel Danis
 */
public class OverallModelDataV046 {

    private PipelineTransferModel donorClf;

    private PipelineTransferModel acceptorClf;

    private ScalingTransferModelV046 scaling;

    private List<Double> threshold;

    public PipelineTransferModel getDonorClf() {
        return donorClf;
    }

    public void setDonorClf(PipelineTransferModel donorClf) {
        this.donorClf = donorClf;
    }

    public PipelineTransferModel getAcceptorClf() {
        return acceptorClf;
    }

    public void setAcceptorClf(PipelineTransferModel acceptorClf) {
        this.acceptorClf = acceptorClf;
    }

    public ScalingTransferModelV046 getScaling() {
        return scaling;
    }

    public void setScaling(ScalingTransferModelV046 scaling) {
        this.scaling = scaling;
    }

    public List<Double> getThreshold() {
        return threshold;
    }

    public void setThreshold(List<Double> threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OverallModelDataV046 that = (OverallModelDataV046) o;
        return Objects.equals(donorClf, that.donorClf) && Objects.equals(acceptorClf, that.acceptorClf) && Objects.equals(scaling, that.scaling) && Objects.equals(threshold, that.threshold);
    }

    @Override
    public int hashCode() {
        return Objects.hash(donorClf, acceptorClf, scaling, threshold);
    }

    @Override
    public String toString() {
        return "OverallModelDataV046{" +
                "donorClf=" + donorClf +
                ", acceptorClf=" + acceptorClf +
                ", scaling=" + scaling +
                ", threshold=" + threshold +
                '}';
    }
}
