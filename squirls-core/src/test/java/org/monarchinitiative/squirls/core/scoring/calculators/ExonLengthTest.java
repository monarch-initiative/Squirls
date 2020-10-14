package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.*;

public class ExonLengthTest extends CalculatorTestBase {

    private ExonLength scorer;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        scorer = new ExonLength(locator);
    }

    @Test
    public void scoreDonorVariant() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1201), "t", "g");

        final double score = scorer.score(variant, st, sequenceInterval);
        assertThat(score, is(closeTo(200., EPSILON)));
    }

    @Test
    public void scoreIntronicVariant() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1207), "a", "g");

        final double score = scorer.score(variant, st, sequenceInterval);
        assertThat(score, is(closeTo(-1., EPSILON)));
    }
}