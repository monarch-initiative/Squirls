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

class WtRiAcceptorTest extends CalculatorTestBase {

    private WtRiAcceptor scorer;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        scorer = new WtRiAcceptor(calculator, generator, locator);
    }

    @Test
    void snpInAcceptor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1399), "g", "a");

        final double score = scorer.score(variant, st, sequenceInterval);

        assertThat(score, is(closeTo(4.1148, EPSILON)));
    }


    @Test
    void notEnoughSequence() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1399), "g", "a");

        final double score = scorer.score(variant, st, sequenceOnOtherChrom);

        assertThat(score, is(notANumber()));
    }
}