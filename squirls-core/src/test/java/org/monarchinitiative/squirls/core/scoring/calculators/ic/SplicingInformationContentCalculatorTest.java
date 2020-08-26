package org.monarchinitiative.squirls.core.scoring.calculators.ic;

import org.jblas.DoubleMatrix;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.PojosForTesting;
import org.monarchinitiative.squirls.core.model.SplicingParameters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

public class SplicingInformationContentCalculatorTest {

    private static final double EPSILON = 0.0001;

    private static DoubleMatrix DONOR_MATRIX, ACCEPTOR_MATRIX;

    private static SplicingParameters SPLICING_PARAMETERS;

    private SplicingInformationContentCalculator instance;

    @BeforeAll
    public static void setUpBeforeAll() {
        DONOR_MATRIX = PojosForTesting.makeDonorMatrix();
        ACCEPTOR_MATRIX = PojosForTesting.makeAcceptorMatrix();
        SPLICING_PARAMETERS = PojosForTesting.makeSplicingParameters();
    }

    @BeforeEach
    public void setUp() {
        instance = new SplicingInformationContentCalculator(DONOR_MATRIX, ACCEPTOR_MATRIX, SPLICING_PARAMETERS);
    }

    @Test
    public void getSpliceDonorScoreTest() {
        assertThat(instance.getSpliceDonorScore("CAGgtaggc"), closeTo(8.66411, EPSILON));
        assertThat(instance.getSpliceDonorScore("TCCgtgagt"), closeTo(3.01706, EPSILON));
        assertThat(instance.getSpliceDonorScore("AAAaaaaaa"), closeTo(-13.77075, EPSILON));
        assertThat(instance.getSpliceDonorScore("CAGXtaggc"), is(Double.NaN));
        assertThat(instance.getSpliceDonorScore("ACGT"), is(Double.NaN));
    }


    @Test
    public void getSpliceAcceptorScoreTest() {
        assertThat(instance.getSpliceAcceptorScore("aggtttttttgaaagtctctcgtagAA"), closeTo(5.44088, EPSILON));
        assertThat(instance.getSpliceAcceptorScore("gctcctttcttaacaggctggaaagTT"), closeTo(-3.37936, EPSILON));
        assertThat(instance.getSpliceAcceptorScore("aaaaaaaaaaaaaaaaaaaaaaaaaAA"), closeTo(-25.48958, EPSILON));
        assertThat(instance.getSpliceAcceptorScore("aggtttttttgaaagtctctcgtagZY"), is(Double.NaN));
        assertThat(instance.getSpliceAcceptorScore("aggtttttttagAA"), is(Double.NaN));
    }
}