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
import static org.mockito.Mockito.*;


class CanonicalDonorScorerTest extends ScorerTestBase {

    @Mock
    private SplicingInformationContentCalculator annotator;

    @Mock
    private AlleleGenerator generator;

    private CanonicalDonorScorer scorer;


    @BeforeEach
    void setUp() {
        scorer = new CanonicalDonorScorer(annotator, generator);
    }

    @Test
    void simpleSnp() {
        when(generator.getDonorSiteWithAltAllele(anyInt(), any(), any())).thenReturn("TCAAATGTA");
        when(annotator.getSpliceDonorScore("TCAAATGTA")).thenReturn(5.0);

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
        assertThat(result, closeTo(0.555, EPSILON));
    }


    @Test
    void wholeDonorSiteIsDeleted() {
        // generator returns null when the whole site is deleted. This is being tested elsewhere
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
        SplicingTernate ternate = SplicingTernate.of(variant, st.getIntrons().get(0), sequenceInterval);
        double result = scorer.scoringFunction().apply(ternate);
        assertThat(result, is(Double.NaN));
    }

    @Test
    void exonIsGivenInsteadOfIntron() {
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
        SplicingTernate ternate = SplicingTernate.of(variant, st.getExons().get(0), sequenceInterval);
        double result = scorer.scoringFunction().apply(ternate);
        assertThat(result, is(Double.NaN));
    }
}