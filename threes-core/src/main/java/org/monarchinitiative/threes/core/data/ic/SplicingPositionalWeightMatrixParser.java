package org.monarchinitiative.threes.core.data.ic;

import org.jblas.DoubleMatrix;
import org.monarchinitiative.threes.core.model.SplicingParameters;

public interface SplicingPositionalWeightMatrixParser {

    SplicingParameters getSplicingParameters();

    DoubleMatrix getDonorMatrix();

    DoubleMatrix getAcceptorMatrix();
}
