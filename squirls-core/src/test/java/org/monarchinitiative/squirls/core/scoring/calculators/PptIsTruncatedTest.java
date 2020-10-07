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

public class PptIsTruncatedTest extends CalculatorTestBase {

    private PptIsTruncated calculator;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        calculator = new PptIsTruncated(locator, ExclusionZoneFeatureCalculator.AGEZ_BEGIN, ExclusionZoneFeatureCalculator.AGEZ_END);
    }

    @ParameterizedTest
    @CsvSource({
            "1384,gcc,g,1.", // match, "gcc" -> "g" deletes 2 cytosines within AGEZ
            "1384,gcc,gaa,1.", // match, "gcc" -> "gaa" deletes 2 cytosines within AGEZ
            // --------------------------------------------------------
            "1391,cgt,c,0.", // non-match, "cgt" -> "c" removes a single thymine within AGEZ
            "1382,cc,c,0.", // non-match, "cc" -> "c" removes a single cytosine from AGEZ
            "1384,g,a,0.", // non-match, this is a SNV and not a deletion
    })
    public void score(int pos, String ref, String alt, double expected) {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, pos), ref, alt);
        final double actual = calculator.score(makeAnnotatable(variant));

        assertThat(actual, is(closeTo(expected, EPSILON)));
    }

}