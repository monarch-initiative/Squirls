package org.monarchinitiative.sss.core;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

class SplicingInformationContentAnnotatorTest {

    private static final double EPSILON = 0.0001;

    private SplicingInformationContentAnnotator instance;


    @Test
    void getSpliceDonorScoreTest() {
        instance = new SplicingInformationContentAnnotator(MakeSplicePositionWeightMatrices.makeDonorMatrix(),
                MakeSplicePositionWeightMatrices.makeAcceptorMatrix());

        assertThat(instance.getSpliceDonorScore("CAGgtaggc"), closeTo(8.66411, EPSILON));
        assertThat(instance.getSpliceDonorScore("TCCgtgagt"), closeTo(3.01706, EPSILON));
        assertThat(instance.getSpliceDonorScore("AAAaaaaaa"), closeTo(-13.77075, EPSILON));
        assertThat(instance.getSpliceDonorScore("CAGXtaggc"), is(Double.NaN));
        assertThat(instance.getSpliceDonorScore("ACGT"), is(Double.NaN));
    }


    @Test
    void getSpliceAcceptorScoreTest() {
        instance = new SplicingInformationContentAnnotator(MakeSplicePositionWeightMatrices.makeDonorMatrix(),
                MakeSplicePositionWeightMatrices.makeAcceptorMatrix());

        assertThat(instance.getSpliceAcceptorScore("aggtttttttgaaagtctctcgtagAA"), closeTo(5.44088, EPSILON));
        assertThat(instance.getSpliceAcceptorScore("gctcctttcttaacaggctggaaagTT"), closeTo(-3.37936, EPSILON));
        assertThat(instance.getSpliceAcceptorScore("aaaaaaaaaaaaaaaaaaaaaaaaaAA"), closeTo(-25.48958, EPSILON));
        assertThat(instance.getSpliceAcceptorScore("aggtttttttgaaagtctctcgtagZY"), is(Double.NaN));
        assertThat(instance.getSpliceAcceptorScore("aggtttttttagAA"), is(Double.NaN));
    }
}