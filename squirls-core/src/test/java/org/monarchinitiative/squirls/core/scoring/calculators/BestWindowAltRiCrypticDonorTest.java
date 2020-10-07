package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.SimpleAnnotatable;
import org.monarchinitiative.squirls.core.scoring.FloatRegion;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.notANumber;

class BestWindowAltRiCrypticDonorTest extends CalculatorTestBase {

    private BestWindowAltRiCrypticDonor scorer;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        scorer = new BestWindowAltRiCrypticDonor(calculator, generator);
    }

    @Test
    void snpInDonor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1200), "g", "a");

        final double score = scorer.score(makeAnnotatable(variant, st));
        assertThat(score, is(closeTo(-2.3987, EPSILON)));
    }

    @Test
    void notEnoughSequence() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1200), "g", "a");

        final SimpleAnnotatable ant = new SimpleAnnotatable(variant,
                st,
                Map.of(
                        FeatureCalculator.FASTA_TRACK_NAME, sequenceOnOtherChrom,
                        FeatureCalculator.PHYLOP_TRACK_NAME, FloatRegion.of(sequenceOnOtherChrom.getInterval(), getRandomScores(sequenceOnOtherChrom.getInterval().length()))
                )
        );
        final double score = scorer.score(ant);
        assertThat(score, is(notANumber()));
    }
}