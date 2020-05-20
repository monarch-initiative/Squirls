package org.monarchinitiative.threes.core.scoring.calculators.sms;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.threes.core.TestDataSourceConfig;
import org.monarchinitiative.threes.core.data.sms.SMSParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;


@SpringBootTest(classes = TestDataSourceConfig.class)
class SMSCalculatorTest {

    private static final double EPSILON = 0.0005;

    @Autowired
    private SMSParser smsParser;

    private SMSCalculator calculator;


    @BeforeEach
    void setUp() {
        calculator = new SMSCalculator(smsParser.getSeptamerMap());
    }


    @ParameterizedTest
    @CsvSource({"AAAAAAA,-0.016", "TTTTTTG,-0.4587", "TTTCACC,0.1455"})
    void scoreSeptamers(String seq, double expected) {
        double s = calculator.scoreSequence(seq);
        assertThat(s, is(closeTo(expected, EPSILON)));
    }


    @ParameterizedTest
    @CsvSource({"CCCCACCTCTTCT,0.2302", "GTTAGGGATGGGA,-1.3347", "TCAGAAGGGCAGA,0.0175"})
    void scoreSequence(String seq, double expected) {
        double s = calculator.scoreSequence(seq);
        assertThat(s, is(closeTo(expected, EPSILON)));
    }


    /**
     * Both upper and lower cases work for nucleotide notation.
     */
    @Test
    void upperAndLowerCasesWork() {
        double s = calculator.scoreSequence("AAAaaaA");
        assertThat(s, is(closeTo(-0.016, EPSILON)));
    }


    /**
     * Minimal length of a sequence to be scored is 7bp.
     */
    @Test
    void scoreShorterSequence() {
        double s = calculator.scoreSequence("AAAAAA");
        assertThat(s, is(Double.NaN));
    }


    @Test
    void invalidNucleotideCharacterLeadsToNan() {
        double s = calculator.scoreSequence("ACGTACGTAAC#TTA");
        assertThat(s, is(Double.NaN));
    }

}