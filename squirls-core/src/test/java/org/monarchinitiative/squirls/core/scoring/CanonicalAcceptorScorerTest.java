package org.monarchinitiative.squirls.core.scoring;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class CanonicalAcceptorScorerTest extends CalculatorTestBase {


    private CanonicalAcceptorFeatureCalculator scorer;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        scorer = new CanonicalAcceptorFeatureCalculator(calculator, generator);
    }

    @Test
    public void snpInAcceptor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1399), "g", "a");
        final GenomePosition anchor = st.getExons().get(1).getInterval().getGenomeBeginPos();

        final double score = scorer.score(anchor, variant, sequenceInterval);

        assertThat(score, is(closeTo(9.9600, EPSILON)));
    }

    @Test
    public void deletionInAcceptor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1397), "cag", "c");
        final GenomePosition anchor = st.getExons().get(1).getInterval().getGenomeBeginPos();

        final double score = scorer.score(anchor, variant, sequenceInterval);

        assertThat(score, is(closeTo(19.4743, EPSILON)));
    }

    @Test
    public void insertionInAcceptor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1399), "g", "gag");
        final GenomePosition anchor = st.getExons().get(1).getInterval().getGenomeBeginPos();

        final double score = scorer.score(anchor, variant, sequenceInterval);

        assertThat(score, is(closeTo(7.9633, EPSILON)));
    }

    @Test
    public void snpJustUpstreamFromAcceptor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1374), "c", "t");
        final GenomePosition anchor = st.getExons().get(1).getInterval().getGenomeBeginPos();

        final double score = scorer.score(anchor, variant, sequenceInterval);

        assertThat(score, is(closeTo(0.0000, EPSILON)));
    }

    @Test
    public void snpJustDownstreamFromAcceptor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1402), "G", "T");
        final GenomePosition anchor = st.getExons().get(1).getInterval().getGenomeBeginPos();

        final double score = scorer.score(anchor, variant, sequenceInterval);

        assertThat(score, is(closeTo(0.0000, EPSILON)));
    }
}