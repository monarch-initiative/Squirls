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

public class BestWindowAltRiCrypticDonorTest extends CalculatorTestBase {

    private BestWindowAltRiCrypticDonor scorer;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        scorer = new BestWindowAltRiCrypticDonor(calculator, generator);
    }

    @Test
    public void snpInDonor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1200), "g", "a");

        final double score = scorer.score(variant, st, sequenceInterval);
        assertThat(score, is(closeTo(-2.3987, EPSILON)));
    }

    @Test
    public void notEnoughSequence() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1200), "g", "a");

        final double score = scorer.score(variant, st, sequenceOnOtherChrom);
        assertThat(score, is(notANumber()));
    }
}