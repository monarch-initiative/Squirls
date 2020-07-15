package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

class CrypticAcceptorScorerTest extends CalculatorTestBase {

    private CrypticAcceptor scorer;


    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        scorer = new CrypticAcceptor(calculator, generator, locator);
    }

    @Test
    void snpInAcceptor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1395), "c", "a");

        final double score = scorer.score(variant, st, sequenceInterval);

        assertThat(score, is(closeTo(0.0000, EPSILON)));
    }

    @Test
    void snpDownstreamFromAcceptor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1404), "G", "A");

        final double score = scorer.score(variant, st, sequenceInterval);

        assertThat(score, is(closeTo(-3.6366, EPSILON)));
    }

    @Test
    void snpUpstreamFromAcceptor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1374), "c", "g");

        final double score = scorer.score(variant, st, sequenceInterval);

        assertThat(score, is(closeTo(-2.0725, EPSILON)));
    }
}