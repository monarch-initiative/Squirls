package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.squirls.core.scoring.calculators.conservation.BigWigAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.mockito.Mockito.when;

public class BigWigTest extends CalculatorTestBase {


    @Autowired
    @Qualifier("referenceDictionary")
    public ReferenceDictionary referenceDictionary;

    @Mock
    public BigWigAccessor accessor;

    private BigWig annotator;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        annotator = new BigWig(accessor);
    }

    @AfterEach
    public void tearDown() throws Exception {
        accessor.close();
    }

    @Test
    public void annotate() throws Exception {
        // arrange
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1201), "t", "g");
        when(accessor.getScores(variant.getGenomeInterval())).thenReturn(List.of(6.03700F));

        // act
        final double score = annotator.score(variant, null, null);

        // assert
        assertThat("Expected score 6.037", score, is(closeTo(6.03700, EPSILON)));
    }

    @Test
    public void annotateDeletion() throws Exception {
        // arrange
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1201), "CTGT", "C");
        when(accessor.getScores(variant.getGenomeInterval())).thenReturn(List.of(.459F, -1.851F, -1.181F));

        // act
        final double score = annotator.score(variant, null, null);

        // assert
        //  chr9:136224582 -  0.459
        //  chr9:136224583 - -1.851
        //  chr9:136224584 - -1.181
        // mean = -0.85766
        assertThat("Expected score -0.857666", score, is(closeTo(-0.85766, EPSILON)));
    }

    @Test
    public void annotateVariantWhenDataIsNotAvailable() throws Exception {
        // arrange
        final GenomePosition pos = new GenomePosition(referenceDictionary, Strand.FWD, 3, 1_000_000, PositionType.ONE_BASED);
        final GenomeVariant variant = new GenomeVariant(pos, "G", "C");
        when(accessor.getScores(variant.getGenomeInterval())).thenReturn(List.of());

        // act
        final double score = annotator.score(variant, null, null);

        // assert
        assertThat(score, is(Double.NaN));
    }
}