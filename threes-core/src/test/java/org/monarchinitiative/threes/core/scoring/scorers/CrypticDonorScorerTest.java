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

class CrypticDonorScorerTest extends ScorerTestBase {

    @Mock
    private SplicingInformationContentCalculator annotator;

    private CrypticDonorScorer scorer;


    @BeforeEach
    void setUp() {
        super.setUp();
        when(annotator.getSplicingParameters()).thenReturn(splicingParameters);
        AlleleGenerator generator = new AlleleGenerator(splicingParameters);
        scorer = new CrypticDonorScorer(annotator, generator);
    }

    @Test
    void snpInDonorSite() {
        // this scorer does not analyze variants overlapping with the donor site
        when(annotator.getSpliceDonorScore(anyString())).thenReturn(5.0);
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1200), "C", "A");
        SplicingTernate ternate = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);

        double result = scorer.scoringFunction().apply(ternate);
        assertThat(result, is(Double.NaN));
    }

    @Test
    void simpleSnpInExon() {
        when(annotator.getSpliceDonorScore(anyString())).thenReturn(5.0);
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1180), "C", "A");
        SplicingTernate ternate = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);

        double result = scorer.scoringFunction().apply(ternate);
        assertThat(result, is(closeTo(-0.555, EPSILON)));
    }


    @Test
    void simpleSnpInIntron() {
        when(annotator.getSpliceDonorScore(anyString())).thenReturn(5.0);
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1230), "C", "A");
        SplicingTernate ternate = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);

        double result = scorer.scoringFunction().apply(ternate);
        assertThat(result, is(closeTo(-0.555, EPSILON)));
    }

    @Test
    void simpleSnpOneBpTooDeepInIntron() {
        when(annotator.getSpliceDonorScore(anyString())).thenReturn(5.0);
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1251), "C", "A");
        SplicingTernate ternate = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);

        double result = scorer.scoringFunction().apply(ternate);
        assertThat(result, is(Double.NaN));
    }
}