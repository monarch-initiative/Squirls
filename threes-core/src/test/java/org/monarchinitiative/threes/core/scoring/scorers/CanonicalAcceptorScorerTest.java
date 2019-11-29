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
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;


class CanonicalAcceptorScorerTest extends ScorerTestBase {

    @Mock
    private SplicingInformationContentCalculator annotator;

    @Mock
    private AlleleGenerator generator;

    private CanonicalAcceptorScorer scorer;


    @BeforeEach
    void setUp() {
        super.setUp();
        when(annotator.getSplicingParameters()).thenReturn(splicingParameters);
        scorer = new CanonicalAcceptorScorer(annotator, generator);
    }

    @Test
    void simpleSnp() {
        when(generator.getAcceptorSiteWithAltAllele(any(), any(), any())).thenReturn("ANY_SEQ");
        when(annotator.getSpliceAcceptorScore("ANY_SEQ")).thenReturn(6.0);

        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1200), "C", "A");
        SplicingTernate ternate = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);
        double result = scorer.scoringFunction().apply(ternate);
        assertThat(result, closeTo(0.666, EPSILON));
    }

    @Test
    void wholeAcceptorSiteIsDeleted() {
        when(generator.getAcceptorSiteWithAltAllele(any(), any(), any())).thenReturn(null);
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1200), "C", "G");

        SplicingTernate ternate = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);
        double result = scorer.scoringFunction().apply(ternate);
        assertThat(result, is(Double.NaN));
    }

    @Test
    void exonIsGivenInsteadOfIntron() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1200), "C", "G");
        SplicingTernate ternate = SplicingTernate.of(variant, st.getExons().get(0), sequenceInterval);
        double result = scorer.scoringFunction().apply(ternate);
        assertThat(result, is(Double.NaN));
    }
}