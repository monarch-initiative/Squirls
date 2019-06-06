package org.monarchinitiative.sss.core.pwm;

import org.jblas.DoubleMatrix;

public interface SplicingPositionalWeightMatrixParser {

    SplicingParameters getSplicingParameters();

    DoubleMatrix getDonorMatrix();

    DoubleMatrix getAcceptorMatrix();
}
