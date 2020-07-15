package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

class BestWindowAltRiCrypticAcceptorTest extends CalculatorTestBase {

    private BestWindowAltRiCrypticAcceptor scorer;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        scorer = new BestWindowAltRiCrypticAcceptor(calculator, generator);
    }

    @Test
    void snpUpstreamFromAcceptorSite() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1374), "c", "g");
        final GenomePosition anchor = st.getExons().get(1).getInterval().getGenomeBeginPos();

        final double score = scorer.score(anchor, variant, sequenceInterval);

        assertThat(score, is(closeTo(2.0423, EPSILON)));
    }
}