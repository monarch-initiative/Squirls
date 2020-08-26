package org.monarchinitiative.squirls.core.data.ic;

import org.jblas.DoubleMatrix;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.PojosForTesting;
import org.monarchinitiative.squirls.core.model.SplicingParameters;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SplicingPwmDataTest {

    private DoubleMatrix donor;

    private DoubleMatrix acceptor;

    private SplicingParameters parameters;

    @BeforeEach
    public void setUp() {
        donor = PojosForTesting.makeFakeDonorMatrix();
        acceptor = PojosForTesting.makeFakeAcceptorMatrix();
        parameters = PojosForTesting.makeFakeSplicingParameters();
    }

    @Test
    public void normalBuild() {
        SplicingPwmData data = SplicingPwmData.builder()
                .setDonor(donor)
                .setAcceptor(acceptor)
                .setParameters(parameters)
                .build();
        assertThat(data.getDonor(), is(donor));
        assertThat(data.getAcceptor(), is(acceptor));
        assertThat(data.getParameters(), is(parameters));
    }

    @Test
    public void failsWhenMissingDonor() {
        assertThrows(NullPointerException.class, () -> SplicingPwmData.builder()
//                .setDonor(donor)
                .setAcceptor(acceptor)
                .setParameters(parameters)
                .build());
    }

    @Test
    public void failsWhenMissingAcceptor() {
        assertThrows(NullPointerException.class, () -> SplicingPwmData.builder()
                .setDonor(donor)
//                .setAcceptor(acceptor)
                .setParameters(parameters)
                .build());
    }

    @Test
    public void failsWhenMissingParameters() {
        assertThrows(NullPointerException.class, () -> SplicingPwmData.builder()
                .setDonor(donor)
                .setAcceptor(acceptor)
//                .setParameters(parameters)
                .build());
    }

    @Test
    public void shortDonor() {
        assertThrows(IllegalArgumentException.class, () -> SplicingPwmData.builder()
                .setDonor(new DoubleMatrix())  // donor with 0 columns
                .setAcceptor(acceptor)
                .setParameters(parameters)
                .build());
    }

    @Test
    public void shortAcceptor() {
        assertThrows(IllegalArgumentException.class, () -> SplicingPwmData.builder()
                .setDonor(donor)
                .setAcceptor(new DoubleMatrix()) // acceptor with 0 columns
                .setParameters(parameters)
                .build());
    }
}