package org.monarchinitiative.threes.core.scoring.scorers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.threes.core.PojosForTesting;
import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.model.GenomeCoordinates;
import org.monarchinitiative.threes.core.model.SplicingParameters;
import org.monarchinitiative.threes.core.model.SplicingTernate;
import org.monarchinitiative.threes.core.model.SplicingVariant;
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
        when(annotator.getSplicingParameters()).thenReturn(splicingParameters);
        AlleleGenerator generator = new AlleleGenerator(splicingParameters);
        scorer = new CrypticAcceptorForVariantsInAcceptorSite(annotator, generator);
        st = PojosForTesting.getTranscriptWithThreeExons();
    }

    @Test
    void snpInAcceptorSite() {
        when(annotator.getSpliceAcceptorScore(anyString())).thenReturn(6.0);
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1399)
                        .setEnd(1400)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("A")
                .build();
        SplicingTernate ternate = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);
        double result = scorer.scoringFunction().apply(ternate);
        assertThat(result, is(closeTo(-6.000, EPSILON)));
    }

    @Test
    void notScoringIndelInAcceptorSite() {
        when(annotator.getSpliceAcceptorScore(anyString())).thenReturn(6.0);
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1398)
                        .setEnd(1400)
                        .setStrand(true)
                        .build())
                .setRef("CC")
                .setAlt("C")
                .build();
        SplicingTernate ternate = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);
        double result = scorer.scoringFunction().apply(ternate);
        assertThat(result, is(Double.NaN));
    }

    @Test
    void notScoringSnpsNotPresentInAcceptorSite() {
        when(annotator.getSpliceAcceptorScore(anyString())).thenReturn(6.0);
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1405)
                        .setEnd(1406)
                        .setStrand(true)
                        .build())
                .setRef("G")
                .setAlt("C")
                .build();
        SplicingTernate ternate = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);
        double result = scorer.scoringFunction().apply(ternate);
        assertThat(result, is(Double.NaN));
    }

}