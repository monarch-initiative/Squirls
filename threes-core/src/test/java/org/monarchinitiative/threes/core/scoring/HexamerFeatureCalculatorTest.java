package org.monarchinitiative.threes.core.scoring;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

class HexamerFeatureCalculatorTest extends CalculatorTestBase {


    @Autowired
    @Qualifier("hexamerMap")
    private Map<String, Double> hexamerMap;

    private HexamerFeatureCalculator calculator;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        calculator = new HexamerFeatureCalculator(hexamerMap);
    }

    @Test
    void score() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1201), "t", "g");
        final double score = calculator.score(null, variant, sequenceInterval);
        assertThat(score, is(closeTo(.837930, EPSILON)));
    }
}