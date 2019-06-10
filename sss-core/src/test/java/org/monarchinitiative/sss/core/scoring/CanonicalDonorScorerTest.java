package org.monarchinitiative.sss.core.scoring;

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
import org.monarchinitiative.sss.core.reference.AlleleStringGenerator;
import org.monarchinitiative.sss.core.reference.SplicingLocationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {TestDataSourceConfig.class})
class CanonicalDonorScorerTest {

    private static final double EPSILON = 0.0005;

    @Autowired
    private SplicingParameters splicingParameters;

    @Mock
    private SplicingInformationContentAnnotator annotator;

    @Mock
    private SplicingTranscriptLocator locator;

    @Mock
    private SequenceInterval si;

    @Mock
    private AlleleStringGenerator generator;

    private CanonicalDonorScorer scorer;

    private SplicingTranscript st;


    @BeforeEach
    void setUp() {
        when(annotator.getSplicingParameters()).thenReturn(splicingParameters);
        scorer = new CanonicalDonorScorer(annotator, locator, generator);
        st = PojosForTesting.getTranscriptWithThreeExons();
    }

    @Test
    void simpleSnp() {
        when(annotator.getSpliceDonorScore(anyString())).thenReturn(5.0);
        when(locator.localize(any(), any())).thenReturn(SplicingLocationData.newBuilder()
                .setFeatureIndex(0)
                .setSplicingPosition(SplicingLocationData.SplicingPosition.DONOR)
                .build());
        when(generator.getDonorSiteWithAltAllele(anyInt(), any(), any())).thenReturn("ANY_SEQ");

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
        double result = scorer.score(variant, st, si);
        assertThat(result, closeTo(0.555, EPSILON));
    }


    @Test
    void notLocatedInDonor() {
        when(annotator.getSpliceDonorScore(anyString())).thenReturn(5.0);
        when(locator.localize(any(), any())).thenReturn(SplicingLocationData.newBuilder()
                .setFeatureIndex(0)
                .setSplicingPosition(SplicingLocationData.SplicingPosition.ACCEPTOR)
                .build());
        when(generator.getDonorSiteWithAltAllele(anyInt(), any(), any())).thenReturn(null);

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
        final double result = scorer.score(variant, st, si);
        assertThat(result, is(Double.NaN));
    }
}