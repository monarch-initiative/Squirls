package org.monarchinitiative.threes.core.scoring.scorers;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.model.SplicingParameters;
import org.monarchinitiative.threes.core.model.SplicingTernate;
import org.monarchinitiative.threes.core.reference.allele.AlleleGenerator;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

class CrypticAcceptorForVariantsInAcceptorSiteTest extends ScorerTestBase {


    private static final double EPSILON = 0.0005;

    @Autowired
    private SplicingParameters splicingParameters;

    @Mock
    private SplicingInformationContentCalculator annotator;

    private CrypticAcceptorForVariantsInAcceptorSite scorer;


    @BeforeEach
    void setUp() {
        super.setUp();
        when(annotator.getSplicingParameters()).thenReturn(splicingParameters);
        AlleleGenerator generator = new AlleleGenerator(splicingParameters);
        scorer = new CrypticAcceptorForVariantsInAcceptorSite(annotator, generator);
    }

    @Test
    void snpInAcceptorSite() {
        when(annotator.getSpliceAcceptorScore(anyString())).thenReturn(6.0);
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1399), "C", "A");
        SplicingTernate ternate = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);

        double result = scorer.scoringFunction().apply(ternate);
        assertThat(result, is(closeTo(-6.000, EPSILON)));
    }

    @Test
    void notScoringIndelInAcceptorSite() {
        when(annotator.getSpliceAcceptorScore(anyString())).thenReturn(6.0);
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1398), "CC", "C");
        SplicingTernate ternate = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);

        double result = scorer.scoringFunction().apply(ternate);
        assertThat(result, is(Double.NaN));
    }

    @Test
    void notScoringSnpsNotPresentInAcceptorSite() {
        when(annotator.getSpliceAcceptorScore(anyString())).thenReturn(6.0);
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1405), "G", "C");
        SplicingTernate ternate = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);

        double result = scorer.scoringFunction().apply(ternate);
        assertThat(result, is(Double.NaN));
    }

}