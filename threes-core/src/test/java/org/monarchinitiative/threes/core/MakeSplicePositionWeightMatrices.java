package org.monarchinitiative.threes.core;

import org.jblas.DoubleMatrix;
import org.monarchinitiative.threes.core.model.SplicingParameters;


public class MakeSplicePositionWeightMatrices {

    private MakeSplicePositionWeightMatrices() {
    }


    // This matrix is expected to be read from the test YAML file at "/genome/dao/splicing/spliceSites.yaml"
    public static DoubleMatrix makeDonorMatrix() {
        return new DoubleMatrix(new double[][]{
                // -3                  +1 (G) +2 (U)                +5     +6
                {0.332, 0.638, 0.097, 0.002, 0.001, 0.597, 0.683, 0.091, 0.179}, // A
                {0.359, 0.107, 0.028, 0.001, 0.012, 0.031, 0.079, 0.060, 0.152}, // C
                {0.186, 0.117, 0.806, 0.996, 0.001, 0.339, 0.122, 0.771, 0.191}, // G
                {0.123, 0.139, 0.069, 0.001, 0.986, 0.032, 0.116, 0.079, 0.478}  // T
        });
    }


    public static DoubleMatrix makeAcceptorMatrix() {
        return new DoubleMatrix(new double[][]{
                {0.248, 0.242, 0.238, 0.228, 0.215, 0.201, 0.181, 0.164, 0.147, 0.136, 0.125, 0.116, 0.106, 0.097, 0.090, 0.091, 0.102, 0.111, 0.117, 0.092, 0.096, 0.240, 0.063, 0.997, 0.001, 0.261, 0.251}, // A
                {0.242, 0.247, 0.250, 0.250, 0.253, 0.255, 0.263, 0.268, 0.271, 0.274, 0.281, 0.278, 0.274, 0.278, 0.259, 0.281, 0.292, 0.319, 0.330, 0.339, 0.293, 0.272, 0.644, 0.001, 0.002, 0.147, 0.191}, // C
                {0.159, 0.155, 0.154, 0.151, 0.151, 0.148, 0.148, 0.142, 0.138, 0.136, 0.128, 0.122, 0.116, 0.107, 0.102, 0.108, 0.114, 0.104, 0.093, 0.066, 0.066, 0.207, 0.006, 0.001, 0.996, 0.478, 0.194}, // G
                {0.350, 0.355, 0.358, 0.371, 0.381, 0.397, 0.409, 0.426, 0.444, 0.454, 0.466, 0.483, 0.504, 0.518, 0.549, 0.519, 0.491, 0.465, 0.460, 0.504, 0.545, 0.281, 0.287, 0.001, 0.001, 0.114, 0.365}  // T
        });
    }

    public static SplicingParameters makeSplicingParameters() {
        return SplicingParameters.builder()
                .setDonorExonic(3)
                .setDonorIntronic(6)
                .setAcceptorExonic(2)
                .setAcceptorIntronic(25)
                .build();
    }
}
