package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.notANumber;

class WtRiDonorTest extends CalculatorTestBase {

    private WtRiDonor scorer;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        scorer = new WtRiDonor(calculator, generator, locator);
    }

    @Test
    void snpInDonor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1200), "g", "a");

        final double score = scorer.score(makeAnnotatable(variant));

        assertThat(score, is(closeTo(3.7028, EPSILON)));
    }

    @Test
    void notEnoughSequence() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1200), "g", "a");

        final double score = scorer.score(makeAnnotatable(variant, st, sequenceOnOtherChrom));
        assertThat(score, is(notANumber()));
    }
}