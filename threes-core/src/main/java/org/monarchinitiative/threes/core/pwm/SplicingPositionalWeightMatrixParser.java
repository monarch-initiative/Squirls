package org.monarchinitiative.threes.core.pwm;

import org.jblas.DoubleMatrix;

public interface SplicingPositionalWeightMatrixParser {

    SplicingParameters getSplicingParameters();

    DoubleMatrix getDonorMatrix();

    DoubleMatrix getAcceptorMatrix();
}
