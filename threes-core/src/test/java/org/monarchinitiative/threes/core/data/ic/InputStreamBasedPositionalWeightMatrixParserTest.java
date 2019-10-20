package org.monarchinitiative.threes.core.data.ic;

import org.jblas.DoubleMatrix;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.MakeSplicePositionWeightMatrices;
import org.monarchinitiative.threes.core.model.SplicingParameters;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InputStreamBasedPositionalWeightMatrixParserTest {

    @Test
    void parseAllTest() throws Exception {
        DoubleMatrix donor, acceptor;
        SplicingParameters parameters;
        // test deserialization of the PWMs from YAML file
        try (InputStream is = InputStreamBasedPositionalWeightMatrixParserTest.class.getResourceAsStream("spliceSites.yaml")) {
            SplicingPositionalWeightMatrixParser instance = new InputStreamBasedPositionalWeightMatrixParser(is);
            donor = instance.getDonorMatrix();
            acceptor = instance.getAcceptorMatrix();
            parameters = instance.getSplicingParameters();
        }

        assertThat(donor, is(MakeSplicePositionWeightMatrices.makeDonorMatrix()));
        assertThat(acceptor, is(MakeSplicePositionWeightMatrices.makeAcceptorMatrix()));
        assertThat(parameters, is(MakeSplicePositionWeightMatrices.makeSplicingParameters()));
    }


    @Test
    void parseMatrixInYamlWithNoRows() throws Exception {
        try (InputStream is = InputStreamBasedPositionalWeightMatrixParserTest.class.getResourceAsStream("spliceSitesNoRows.yaml")) {
            assertThrows(IllegalArgumentException.class, () -> new InputStreamBasedPositionalWeightMatrixParser(is));
        }
    }


    @Test
    void parseMatrixInYamlWithInvalidRowCount() throws Exception {
        try (InputStream is = InputStreamBasedPositionalWeightMatrixParserTest.class.getResourceAsStream("spliceSitesBadRowCount.yaml")) {
            assertThrows(IllegalArgumentException.class, () -> new InputStreamBasedPositionalWeightMatrixParser(is));

        }
    }


    @Test
    void parseMatrixWhereColumnDoesNotSumToOne() throws Exception {
        try (InputStream is = InputStreamBasedPositionalWeightMatrixParserTest.class.getResourceAsStream("spliceSitesDoesNotSumTo1.yaml")) {
            assertThrows(IllegalArgumentException.class, () -> new InputStreamBasedPositionalWeightMatrixParser(is));
        }
    }


    @Test
    void parseMatrixWhereRowsHaveDifferentSize() throws Exception {
        try (InputStream is = InputStreamBasedPositionalWeightMatrixParserTest.class.getResourceAsStream("spliceSitesRowsWithDifferentSize.yaml")) {
            assertThrows(IllegalArgumentException.class, () -> new InputStreamBasedPositionalWeightMatrixParser(is));
        }
    }
}