package org.monarchinitiative.sss.core.scoring.scorers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.sss.core.PojosForTesting;
import org.monarchinitiative.sss.core.TestDataSourceConfig;
import org.monarchinitiative.sss.core.model.GenomeCoordinates;
import org.monarchinitiative.sss.core.model.SequenceInterval;
import org.monarchinitiative.sss.core.model.SplicingTranscript;
import org.monarchinitiative.sss.core.model.SplicingVariant;
import org.monarchinitiative.sss.core.pwm.SplicingInformationContentAnnotator;
import org.monarchinitiative.sss.core.pwm.SplicingParameters;
import org.monarchinitiative.sss.core.reference.allele.AlleleGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {TestDataSourceConfig.class})
class CrypticAcceptorScorerTest {


    private static final double EPSILON = 0.0005;

    private static SplicingTranscript st = PojosForTesting.getTranscriptWithThreeExons();

    @Autowired
    private SplicingParameters splicingParameters;

    @Mock
    private SplicingInformationContentAnnotator annotator;

    @Mock
    private SequenceInterval sequenceInterval;

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
        double result = scorer.score(variant, st.getIntrons().get(0), null);
        assertThat(result, is(Double.NaN));
    }

    @Test
    void simpleSnpInExon() {
        when(annotator.getSpliceAcceptorScore(anyString())).thenReturn(6.0);
        when(sequenceInterval.getSubsequence(anyInt(), anyInt())).thenReturn("ACGTACGTTACGTACGTTACGTACGT");

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
        double result = scorer.score(variant, st.getIntrons().get(0), sequenceInterval);
        assertThat(result, is(closeTo(-0.666, EPSILON)));
    }

    @Test
    void simpleSnpInIntron() {
        when(annotator.getSpliceAcceptorScore(anyString())).thenReturn(6.0);
        when(sequenceInterval.getSubsequence(anyInt(), anyInt())).thenReturn("ACGTACGTTACGTACGTTACGTACGT");

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
        double result = scorer.score(variant, st.getIntrons().get(0), sequenceInterval);
        assertThat(result, is(closeTo(-0.666, EPSILON)));
    }

    @Test
    void simpleSnpOneBpTooDeepInIntron() {
        when(annotator.getSpliceAcceptorScore(anyString())).thenReturn(6.0);
        when(sequenceInterval.getSubsequence(anyInt(), anyInt())).thenReturn("ACGTACGTTACGTACGTTACGTACGT");

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
        double result = scorer.score(variant, st.getIntrons().get(0), sequenceInterval);
        assertThat(result, is(Double.NaN));
    }

}