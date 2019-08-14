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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CrypticAcceptorScorerTest extends ScorerTestBase {


    @Mock
    private SplicingInformationContentCalculator annotator;


    private CrypticAcceptorScorer scorer;

    @BeforeEach
    void setUp() {
        when(annotator.getSplicingParameters()).thenReturn(splicingParameters);
        AlleleGenerator generator = new AlleleGenerator(splicingParameters);
        scorer = new CrypticAcceptorScorer(annotator, generator);
    }

    @Test
    void snpInAcceptorSite() {
        when(annotator.getSpliceAcceptorScore(anyString())).thenReturn(5.0);

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
        final SplicingTernate t = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);
        double result = scorer.scoringFunction().apply(t);
        assertThat(result, is(Double.NaN));
    }

    @Test
    void simpleSnpInExon() {
        when(annotator.getSpliceAcceptorScore(anyString())).thenReturn(6.0);

        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1410)
                        .setEnd(1411)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("A")
                .build();
        final SplicingTernate t = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);
        double result = scorer.scoringFunction().apply(t);
        assertThat(result, is(closeTo(-0.666, EPSILON)));
    }

    @Test
    void simpleSnpInIntron() {
        when(annotator.getSpliceAcceptorScore(anyString())).thenReturn(6.0);


        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1360)
                        .setEnd(1361)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("A")
                .build();
        final SplicingTernate t = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);
        double result = scorer.scoringFunction().apply(t);
        assertThat(result, is(closeTo(-0.666, EPSILON)));
    }

    @Test
    void simpleSnpOneBpTooDeepInIntron() {
        when(annotator.getSpliceAcceptorScore(anyString())).thenReturn(6.0);

        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1324)
                        .setEnd(1325)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("A")
                .build();
        final SplicingTernate t = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);
        double result = scorer.scoringFunction().apply(t);
        assertThat(result, is(Double.NaN));
    }

}