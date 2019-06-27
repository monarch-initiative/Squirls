package org.monarchinitiative.threes.core.scoring.scorers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.model.GenomeCoordinates;
import org.monarchinitiative.threes.core.model.SplicingTernate;
import org.monarchinitiative.threes.core.model.SplicingVariant;
import org.monarchinitiative.threes.core.reference.allele.AlleleGenerator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;


class CanonicalAcceptorScorerTest extends ScorerTestBase {

    @Mock
    private SplicingInformationContentCalculator annotator;

    @Mock
    private AlleleGenerator generator;

    private CanonicalAcceptorScorer scorer;


    @BeforeEach
    void setUp() {
        when(annotator.getSplicingParameters()).thenReturn(splicingParameters);
        scorer = new CanonicalAcceptorScorer(annotator, generator);
    }

    @Test
    void simpleSnp() {
        when(annotator.getSpliceAcceptorScore(anyString())).thenReturn(6.0);
        when(generator.getAcceptorSiteWithAltAllele(anyInt(), any(), any())).thenReturn("ANY_SEQ");

        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1200)
                        .setEnd(1201)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("A")
                .build();
        SplicingTernate ternate = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);
        double result = scorer.scoringFunction().apply(ternate);
        assertThat(result, closeTo(0.666, EPSILON));
    }

    @Test
    void notLocatedInDonor() {
        when(annotator.getSpliceAcceptorScore(anyString())).thenReturn(5.0);
        when(generator.getAcceptorSiteWithAltAllele(anyInt(), any(), any())).thenReturn(null);

        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1200)
                        .setEnd(1201)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("A")
                .build();
        SplicingTernate ternate = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);
        double result = scorer.scoringFunction().apply(ternate);
        assertThat(result, is(Double.NaN));
    }
}