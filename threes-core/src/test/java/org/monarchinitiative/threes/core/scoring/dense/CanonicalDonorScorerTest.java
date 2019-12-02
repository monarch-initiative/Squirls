package org.monarchinitiative.threes.core.scoring.dense;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.scoring.ScorerTestBase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

class CanonicalDonorScorerTest extends ScorerTestBase {

    private CanonicalDonorScorer scorer;


    @BeforeEach
    public void setUp() {
        super.setUp();
        scorer = new CanonicalDonorScorer(calculator, generator);
    }

    @Test
    void snpInDonor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1200), "g", "a");
        final GenomePosition anchor = st.getExons().get(0).getInterval().getGenomeEndPos();
        final double score = scorer.score(anchor, variant, sequenceInterval);
        assertThat(score, is(closeTo(8.9600, EPSILON)));
    }

    @Test
    void deletionInDonor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1199), "Ggt", "G");
        final GenomePosition anchor = st.getExons().get(0).getInterval().getGenomeEndPos();
        final double score = scorer.score(anchor, variant, sequenceInterval);
        assertThat(score, is(closeTo(15.6686, EPSILON)));
    }

    @Test
    void insertionInDonor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1200), "gt", "gtgt");
        final GenomePosition anchor = st.getExons().get(0).getInterval().getGenomeEndPos();
        final double score = scorer.score(anchor, variant, sequenceInterval);
        assertThat(score, is(closeTo(-0.6725, EPSILON)));
    }

    @Test
    void snpJustUpstreamFromDonor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1196), "G", "A");
        final GenomePosition anchor = st.getExons().get(0).getInterval().getGenomeEndPos();
        final double score = scorer.score(anchor, variant, sequenceInterval);
        assertThat(score, is(closeTo(0.0000, EPSILON)));
    }

    @Test
    void snpJustDownstreamFromDonor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1206), "a", "c");
        final GenomePosition anchor = st.getExons().get(0).getInterval().getGenomeEndPos();
        final double score = scorer.score(anchor, variant, sequenceInterval);
        assertThat(score, is(closeTo(0.0000, EPSILON)));
    }
}