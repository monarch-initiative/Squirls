package org.monarchinitiative.threes.core.scoring.scorers;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.model.SplicingTernate;
import org.monarchinitiative.threes.core.reference.allele.AlleleGenerator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CrypticAcceptorScorerTest extends ScorerTestBase {


    @Mock
    private SplicingInformationContentCalculator annotator;


    private CrypticAcceptorScorer scorer;

    @BeforeEach
    void setUp() {
        super.setUp();
        when(annotator.getSplicingParameters()).thenReturn(splicingParameters);
        AlleleGenerator generator = new AlleleGenerator(splicingParameters);
        scorer = new CrypticAcceptorScorer(annotator, generator);
    }

    @Test
    void snpInAcceptorSite() {
        when(annotator.getSpliceAcceptorScore(anyString())).thenReturn(5.0);
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1399), "C", "A");
        final SplicingTernate t = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);

        double result = scorer.scoringFunction().apply(t);
        assertThat(result, is(Double.NaN));
    }

    @Test
    void simpleSnpInExon() {
        when(annotator.getSpliceAcceptorScore(anyString())).thenReturn(6.0);
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1410), "C", "A");
        final SplicingTernate t = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);

        double result = scorer.scoringFunction().apply(t);
        assertThat(result, is(closeTo(-0.666, EPSILON)));
    }

    @Test
    void simpleSnpInIntron() {
        when(annotator.getSpliceAcceptorScore(anyString())).thenReturn(6.0);
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1360), "C", "A");
        final SplicingTernate t = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);

        double result = scorer.scoringFunction().apply(t);
        assertThat(result, is(closeTo(-0.666, EPSILON)));
    }

    @Test
    void simpleSnpOneBpTooDeepInIntron() {
        when(annotator.getSpliceAcceptorScore(anyString())).thenReturn(6.0);
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1349), "C", "A");
        final SplicingTernate t = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);

        double result = scorer.scoringFunction().apply(t);
        assertThat(result, is(Double.NaN));
    }

}