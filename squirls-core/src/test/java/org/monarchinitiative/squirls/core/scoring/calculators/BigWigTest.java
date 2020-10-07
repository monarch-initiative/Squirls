package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;

class BigWigTest extends CalculatorTestBase {


    @Autowired
    @Qualifier("referenceDictionary")
    private ReferenceDictionary referenceDictionary;


    private BigWig annotator;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        annotator = new BigWig();
    }

    @AfterEach
    void tearDown() throws Exception {

    }

    @Test
    void annotate() throws Exception {
        // arrange
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1201), "t", "g");

        // act
        final double score = annotator.score(makeAnnotatable(variant));

        // assert
        assertThat("Expected score 0.8862130", score, is(closeTo(0.871396, EPSILON)));
    }

    @Test
    void annotateDeletion() throws Exception {
        // arrange
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1201), "CTGT", "C");

        // act
        final double score = annotator.score(makeAnnotatable(variant));

        // assert
        assertThat("Expected score 0.390458", score, is(closeTo(0.496073, EPSILON)));
    }

    @Test
    void annotateVariantWhenDataIsNotAvailable() throws Exception {
        // arrange
        final GenomePosition pos = new GenomePosition(referenceDictionary, Strand.FWD, 3, 1_000_000, PositionType.ONE_BASED);
        final GenomeVariant variant = new GenomeVariant(pos, "G", "C");

        // act
        final double score = annotator.score(makeAnnotatable(variant));

        // assert
        assertThat(score, is(Double.NaN));
    }
}