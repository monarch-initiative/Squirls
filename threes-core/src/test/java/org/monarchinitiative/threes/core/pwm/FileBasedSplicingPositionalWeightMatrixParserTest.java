package org.monarchinitiative.threes.core.pwm;

import org.jblas.DoubleMatrix;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.MakeSplicePositionWeightMatrices;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileBasedSplicingPositionalWeightMatrixParserTest {

    @Test
    void parseAllTest() throws Exception {
        DoubleMatrix donor, acceptor;
        SplicingParameters parameters;
        // test deserialization of the PWMs from YAML file
        try (InputStream is = FileBasedSplicingPositionalWeightMatrixParserTest.class.getResourceAsStream("spliceSites.yaml")) {
            SplicingPositionalWeightMatrixParser instance = new FileBasedSplicingPositionalWeightMatrixParser(is);
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
        try (InputStream is = FileBasedSplicingPositionalWeightMatrixParserTest.class.getResourceAsStream("spliceSitesNoRows.yaml")) {
            assertThrows(IllegalArgumentException.class, () -> new FileBasedSplicingPositionalWeightMatrixParser(is));
        }
    }


    @Test
    void parseMatrixInYamlWithInvalidRowCount() throws Exception {
        try (InputStream is = FileBasedSplicingPositionalWeightMatrixParserTest.class.getResourceAsStream("spliceSitesBadRowCount.yaml")) {
            assertThrows(IllegalArgumentException.class, () -> new FileBasedSplicingPositionalWeightMatrixParser(is));

        }
    }


    @Test
    void parseMatrixWhereColumnDoesNotSumToOne() throws Exception {
        try (InputStream is = FileBasedSplicingPositionalWeightMatrixParserTest.class.getResourceAsStream("spliceSitesDoesNotSumTo1.yaml")) {
            assertThrows(IllegalArgumentException.class, () -> new FileBasedSplicingPositionalWeightMatrixParser(is));
        }
    }


    @Test
    void parseMatrixWhereRowsHaveDifferentSize() throws Exception {
        try (InputStream is = FileBasedSplicingPositionalWeightMatrixParserTest.class.getResourceAsStream("spliceSitesRowsWithDifferentSize.yaml")) {
            assertThrows(IllegalArgumentException.class, () -> new FileBasedSplicingPositionalWeightMatrixParser(is));
        }
    }
}