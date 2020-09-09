package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class PyrimidineToPurineAtMinusThreeTest extends CalculatorTestBase {

    private PyrimidineToPurineAtMinusThree calculator;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        calculator = new PyrimidineToPurineAtMinusThree(locator, generator);
    }

    @ParameterizedTest
    @CsvSource({
            // matches
            "1397,c,a,1.", // "c" -> "a" converts Y to R at -3 position
            "1396,gc,g,1.", // "gc" -> "g" deletes cytosine at -3 and effectively converts g at -4 to R at -3 position
            "1397,c,ca,1.", // "c" -> "ca" inserts R to -3 position
            "1395,cgc,cag,1.", // "cgc" -> "cag" replaces Y with R at -3 position
            // --------------------------------------------------------
            // non-matches
            "1397,c,t,0.", // "c" -> "t" retains pyrimidine at -3 position
            "1394,ccgc,c,0.", // the deletion retains pyrimidine at -3 position
            "1395,cgc,ctt,0.", // "cgc" -> "ctt" retains Y at -3 position
            "1402,G,T,0.", // not located within the acceptor site
    })
    public void score(int pos, String ref, String alt, double expected) {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, pos), ref, alt);
        final double actual = calculator.score(variant, st, sequenceInterval);

        assertThat(actual, is(closeTo(expected, EPSILON)));
    }

}