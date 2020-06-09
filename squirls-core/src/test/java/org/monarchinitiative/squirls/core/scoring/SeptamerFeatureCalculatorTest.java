package org.monarchinitiative.squirls.core.scoring;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

class SeptamerFeatureCalculatorTest extends CalculatorTestBase {

    @Qualifier("septamerMap")
    @Autowired
    private Map<String, Double> septamerMap;


    private SeptamerFeatureCalculator calculator;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        calculator = new SeptamerFeatureCalculator(septamerMap);
    }

    @Test
    void score() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1201), "t", "g");
        final double score = calculator.score(null, variant, sequenceInterval);
        assertThat(score, is(closeTo(.317399, EPSILON)));
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