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
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {TestDataSourceConfig.class})
class CrypticDonorForVariantsInDonorSiteTest {


    private static final double EPSILON = 0.0005;

    @Autowired
    private SplicingParameters splicingParameters;

    @Mock
    private SplicingInformationContentAnnotator annotator;

    @Mock
    private SequenceInterval sequenceInterval;

    private CrypticDonorForVariantsInDonorSite scorer;

    private SplicingTranscript st;


    @BeforeEach
    void setUp() {
        when(annotator.getSplicingParameters()).thenReturn(splicingParameters);
        AlleleGenerator generator = new AlleleGenerator(splicingParameters);
        scorer = new CrypticDonorForVariantsInDonorSite(annotator, generator);
        st = PojosForTesting.getTranscriptWithThreeExons();
    }

    @Test
    void snpInDonorSite() {
        when(annotator.getSpliceDonorScore(anyString())).thenReturn(5.0);
        when(sequenceInterval.getSubsequence(anyInt(), anyInt())).thenReturn("ACGTACGTA");
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
        double result = scorer.score(variant, st.getIntrons().get(0), sequenceInterval);
        assertThat(result, is(closeTo(-5.000, EPSILON)));
    }

    @Test
    void notScoringIndelInDonorSite() {
        when(annotator.getSpliceDonorScore(anyString())).thenReturn(5.0);
        when(sequenceInterval.getSubsequence(anyInt(), anyInt())).thenReturn("ACGTACGTA");
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1200)
                        .setEnd(1202)
                        .setStrand(true)
                        .build())
                .setRef("CC")
                .setAlt("C")
                .build();
        double result = scorer.score(variant, st.getIntrons().get(0), sequenceInterval);
        assertThat(result, is(Double.NaN));
    }

    @Test
    void notScoringSnpsNotPresentInDonorSite() {
        when(annotator.getSpliceDonorScore(anyString())).thenReturn(5.0);
        when(sequenceInterval.getSubsequence(anyInt(), anyInt())).thenReturn("ACGTACGTA");
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1210)
                        .setEnd(1211)
                        .setStrand(true)
                        .build())
                .setRef("G")
                .setAlt("C")
                .build();
        double result = scorer.score(variant, st.getIntrons().get(0), sequenceInterval);
        assertThat(result, is(Double.NaN));
    }


}