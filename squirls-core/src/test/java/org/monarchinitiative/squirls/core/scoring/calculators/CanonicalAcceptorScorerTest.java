package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

class CanonicalAcceptorScorerTest extends CalculatorTestBase {


    private CanonicalAcceptor scorer;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        scorer = new CanonicalAcceptor(calculator, generator, locator);
    }

    @Test
    void snpInAcceptor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1399), "g", "a");

        final double score = scorer.score(variant, st, sequenceInterval);

        assertThat(score, is(closeTo(9.9600, EPSILON)));
    }

    @Test
    void deletionInAcceptor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1397), "cag", "c");

        final double score = scorer.score(variant, st, sequenceInterval);

        assertThat(score, is(closeTo(19.4743, EPSILON)));
    }

    @Test
    void insertionInAcceptor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1399), "g", "gag");

        final double score = scorer.score(variant, st, sequenceInterval);

        assertThat(score, is(closeTo(7.9633, EPSILON)));
    }

    @Test
    void snpJustUpstreamFromAcceptor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1374), "c", "t");

        final double score = scorer.score(variant, st, sequenceInterval);

        assertThat(score, is(closeTo(0.0000, EPSILON)));
    }

    @Test
    void snpJustDownstreamFromAcceptor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1402), "G", "T");

        final double score = scorer.score(variant, st, sequenceInterval);

        assertThat(score, is(closeTo(0.0000, EPSILON)));
    }
}