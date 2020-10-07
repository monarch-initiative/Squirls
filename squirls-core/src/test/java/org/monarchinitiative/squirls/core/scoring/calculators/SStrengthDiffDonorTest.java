package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

class SStrengthDiffDonorTest extends CalculatorTestBase {

    private SStrengthDiffDonor scorer;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        scorer = new SStrengthDiffDonor(calculator, generator, locator);
    }

    @Test
    void variantInDonorOfTheFirstExon() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1200), "g", "a");

        final double score = scorer.score(makeAnnotatable(variant));
        assertThat(score, is(closeTo(-8.1511, EPSILON)));
    }

    @Test
    void variantInDonorOfTheSecondExon() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1600), "g", "c");

        final double score = scorer.score(makeAnnotatable(variant));
        assertThat(score, is(closeTo(0., EPSILON)));
    }
}