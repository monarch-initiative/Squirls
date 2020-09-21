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


public class ExclusionZoneFeatureCalculatorTest extends CalculatorTestBase {

    private ExclusionZoneFeatureCalculator agCalculator;

    private ExclusionZoneFeatureCalculator yagCalculator;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        agCalculator = ExclusionZoneFeatureCalculator.makeAgCalculator(locator, ExclusionZoneFeatureCalculator.AGEZ_BEGIN, ExclusionZoneFeatureCalculator.AGEZ_END);
        yagCalculator = ExclusionZoneFeatureCalculator.makeYagCalculator(locator, ExclusionZoneFeatureCalculator.AGEZ_BEGIN, ExclusionZoneFeatureCalculator.AGEZ_END);
    }

    @ParameterizedTest
    @CsvSource({
            "1383,c,a,1.", // match, "ccg" -> "cag" within AGEZ
            "1389,c,a,1.", // match, "acg" -> "agg" within AGEZ
            "1388,ac,a,1.", // match, turns "acg" -> "ag" but at coding position of the first exon (no acceptor)
            "1389,c,cag,1.", // match, turns "c" -> "cag" within AGEZ
            // --------------------------------------------------------
            "1198,A,G,0.", // non-match, turns "AAG" -> "AGG" but at coding position of the first exon (no acceptor)
            "1399,g,a,0.", // non-match, turns "agG" -> "aaG" but the position is not within AGEZ
            "1389,c,t,0.", // non-match, turns "acg" -> "atg" within AGEZ
    })
    public void agScore(int pos, String ref, String alt, double expected) {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, pos), ref, alt);
        final double actual = agCalculator.score(variant, st, sequenceInterval);

        assertThat(actual, is(closeTo(expected, EPSILON)));
    }


    @ParameterizedTest
    @CsvSource({
            // match
            "1389,c,cag,1.", // match, turns "c" -> "cag" within AGEZ
            "1383,c,a,1.", // "ccg" -> "cag" within AGEZ
            "1388,ac,a,1.", // match, turns "c acg" -> "c ag" but at coding position of the first exon (no acceptor)

            // --------------------------------------------------------
            // non-match
            "1389,c,a,0.", // "acg" -> "agg" within AGEZ
            "1198,A,G,0.", // non-match, turns "AAG" -> "AGG" but at coding position of the first exon (no acceptor)
            "1399,g,a,0.", // non-match, turns "agG" -> "aaG" but the position is not within AGEZ
            "1389,c,t,0.", // non-match, turns "acg" -> "atg" within AGEZ
    })
    public void yagScore(int pos, String ref, String alt, double expected) {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, pos), ref, alt);
        final double actual = yagCalculator.score(variant, st, sequenceInterval);

        assertThat(actual, is(closeTo(expected, EPSILON)));
    }

}