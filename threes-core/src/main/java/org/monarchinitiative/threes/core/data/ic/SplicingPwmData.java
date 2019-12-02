package org.monarchinitiative.threes.core.data.ic;

import org.jblas.DoubleMatrix;
import org.monarchinitiative.threes.core.model.SplicingParameters;

import java.util.Objects;

/**
 * POJO for holding:
 * <ul>
 *     <li>{@link DoubleMatrix} for <em>donor</em> site</li>
 *     <li>{@link DoubleMatrix} for <em>acceptor</em> site, and</li>
 *     <li>{@link SplicingParameters} with data regarding both splice donor and acceptor sites</li>
 * </ul>
 */
public class SplicingPwmData {

    private final DoubleMatrix donor;

    private final DoubleMatrix acceptor;

    private final SplicingParameters parameters;

    private SplicingPwmData(Builder builder) {
        // first check for nulls
        donor = Objects.requireNonNull(builder.donor);
        acceptor = Objects.requireNonNull(builder.acceptor);
        parameters = Objects.requireNonNull(builder.parameters);

        // then perform more specific checks
        if (donor.columns != parameters.getDonorLength()) {
            throw new IllegalArgumentException(
                    String.format("Length of donor in matrix (%d) and parameters (%d) do not match",
                            donor.columns, parameters.getDonorLength()));
        }
        if (acceptor.columns != parameters.getAcceptorLength()) {
            throw new IllegalArgumentException(
                    String.format("Length of acceptor in matrix (%d) and parameters (%d) do not match",
                            acceptor.columns, parameters.getAcceptorLength()));
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public DoubleMatrix getDonor() {
        return donor;
    }

    public DoubleMatrix getAcceptor() {
        return acceptor;
    }

    public SplicingParameters getParameters() {
        return parameters;
    }

    public static final class Builder {

        private DoubleMatrix donor;

        private DoubleMatrix acceptor;

        private SplicingParameters parameters;

        private Builder() {
        }

        public Builder setDonor(DoubleMatrix donor) {
            this.donor = donor;
            return this;
        }

        public Builder setAcceptor(DoubleMatrix acceptor) {
            this.acceptor = acceptor;
            return this;
        }

        public Builder setParameters(SplicingParameters parameters) {
            this.parameters = parameters;
            return this;
        }

        public SplicingPwmData build() {
            return new SplicingPwmData(this);
        }
    }
}
