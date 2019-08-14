package org.monarchinitiative.threes.core.calculators.sms;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

class SMSCalculatorTest {

    private static final double EPSILON = 0.0005;

    /**
     * Path to file with real scores
     */
    private static Path SEPTAMER_TSV_PATH = Paths.get(SMSCalculatorTest.class.getResource("septamer-scores.tsv").getPath());

    private static Map<String, Double> SEPTA_MAP;

    private SMSCalculator calculator;

    @BeforeAll
    static void setUpBefore() throws IOException {
        FileSMSParser parser = new FileSMSParser(SEPTAMER_TSV_PATH);
        SEPTA_MAP = parser.getSeptamerMap();

    }

    @BeforeEach
    void setUp() {
        calculator = new SMSCalculator(SEPTA_MAP);
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