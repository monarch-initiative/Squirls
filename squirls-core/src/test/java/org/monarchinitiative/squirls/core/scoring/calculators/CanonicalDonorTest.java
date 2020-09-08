package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

class CanonicalDonorTest extends CalculatorTestBase {

    private CanonicalDonor scorer;


    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        scorer = new CanonicalDonor(calculator, generator, locator);
    }

    @Test
    void snpInDonor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1200), "g", "a");

        final double score = scorer.score(variant, st, sequenceInterval);
        assertThat(score, is(closeTo(8.9600, EPSILON)));
    }

    @Test
    void deletionInDonor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1199), "Ggt", "G");

        final double score = scorer.score(variant, st, sequenceInterval);
        assertThat(score, is(closeTo(15.6686, EPSILON)));
    }

    @Test
    void insertionInDonor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1200), "gt", "gtgt");

        final double score = scorer.score(variant, st, sequenceInterval);
        assertThat(score, is(closeTo(-0.6725, EPSILON)));
    }

    @Test
    void snpJustUpstreamFromDonor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1196), "G", "A");

        final double score = scorer.score(variant, st, sequenceInterval);
        assertThat(score, is(closeTo(0.0000, EPSILON)));
    }

    @Test
    void snpJustDownstreamFromDonor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1206), "a", "c");

        final double score = scorer.score(variant, st, sequenceInterval);
        assertThat(score, is(closeTo(0.0000, EPSILON)));
    }
}