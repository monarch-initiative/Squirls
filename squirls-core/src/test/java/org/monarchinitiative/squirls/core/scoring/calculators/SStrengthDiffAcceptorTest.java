package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

class SStrengthDiffAcceptorTest extends CalculatorTestBase {

    private SStrengthDiffAcceptor scorer;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        scorer = new SStrengthDiffAcceptor(calculator, generator, locator);
    }

    @Test
    void variantInAcceptorOfTheSecondExon() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1399), "g", "a");

        final double score = scorer.score(makeAnnotatable(variant));
        assertThat(score, is(closeTo(-16.2526, EPSILON)));
    }

    @Test
    void variantInAcceptorOfTheLastExon() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1799), "g", "c");

        final double score = scorer.score(makeAnnotatable(variant));
        assertThat(score, is(closeTo(0., EPSILON)));
    }
}