package org.monarchinitiative.threes.core.scoring.scorers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.model.GenomeCoordinates;
import org.monarchinitiative.threes.core.model.SplicingParameters;
import org.monarchinitiative.threes.core.model.SplicingTernate;
import org.monarchinitiative.threes.core.model.SplicingVariant;
import org.monarchinitiative.threes.core.reference.allele.AlleleGenerator;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

class CrypticDonorForVariantsInDonorSiteTest extends ScorerTestBase {


    private static final double EPSILON = 0.0005;

    @Autowired
    private SplicingParameters splicingParameters;

    @Mock
    private SplicingInformationContentCalculator annotator;

    private CrypticDonorForVariantsInDonorSite scorer;


    @BeforeEach
    void setUp() {
        when(annotator.getSplicingParameters()).thenReturn(splicingParameters);
        AlleleGenerator generator = new AlleleGenerator(splicingParameters);
        scorer = new CrypticDonorForVariantsInDonorSite(annotator, generator);
    }

    @Test
    void snpInDonorSite() {
        when(annotator.getSpliceDonorScore(anyString())).thenReturn(5.0);
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
        assertThat(result, is(closeTo(-5.000, EPSILON)));
    }

    @Test
    void notScoringIndelInDonorSite() {
        when(annotator.getSpliceDonorScore(anyString())).thenReturn(5.0);
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
        SplicingTernate ternate = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);
        double result = scorer.scoringFunction().apply(ternate);
        assertThat(result, is(Double.NaN));
    }

    @Test
    void notScoringSnpsNotPresentInDonorSite() {
        when(annotator.getSpliceDonorScore(anyString())).thenReturn(5.0);
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
        SplicingTernate ternate = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);
        double result = scorer.scoringFunction().apply(ternate);
        assertThat(result, is(Double.NaN));
    }


}