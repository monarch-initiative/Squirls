package org.monarchinitiative.squirls.core.data.ic;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.PojosForTesting;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InputStreamBasedPositionalWeightMatrixParserTest {

    @Test
    void parseAllTest() throws Exception {
        SplicingPwmData splicingPwmData;

        // test deserialization of the PWMs from YAML file
        try (InputStream is = InputStreamBasedPositionalWeightMatrixParserTest.class.getResourceAsStream("spliceSites.yaml")) {
            SplicingPositionalWeightMatrixParser instance = new InputStreamBasedPositionalWeightMatrixParser(is);
            splicingPwmData = instance.getSplicingPwmData();
        }


        assertThat(splicingPwmData.getDonor(), is(PojosForTesting.makeDonorMatrix()));
        assertThat(splicingPwmData.getAcceptor(), is(PojosForTesting.makeAcceptorMatrix()));
        assertThat(splicingPwmData.getParameters(), is(PojosForTesting.makeSplicingParameters()));
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