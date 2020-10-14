package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class CrypticDonorScorerTest extends CalculatorTestBase {

    private CrypticDonor scorer;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        scorer = new CrypticDonor(calculator, generator, locator);
    }

    @Test
    public void snpInDonor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1201), "t", "g");

        final double score = scorer.score(variant, st, sequenceInterval);

        assertThat(score, is(closeTo(4.6317, EPSILON)));
    }

    @Test
    public void snpUpstreamFromDonor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1196), "C", "T");

        final double score = scorer.score(variant, st, sequenceInterval);

        assertThat(score, is(closeTo(0.3526, EPSILON)));
    }
}