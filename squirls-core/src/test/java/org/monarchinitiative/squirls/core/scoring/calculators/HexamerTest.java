package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

class HexamerTest extends CalculatorTestBase {


    @Autowired
    @Qualifier("hexamerMap")
    private Map<String, Double> hexamerMap;

    private Hexamer calculator;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        calculator = new Hexamer(hexamerMap);
    }

    @Test
    void score() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1201), "t", "g");
        final double score = calculator.score(variant, st, sequenceInterval);
        assertThat(score, is(closeTo(.837930, EPSILON)));
    }

    /**
     * Test variant from <i>DiGiacomo et al. 2013 - Functional Analysis of a Large set of BRCA2 exon 7 Variants
     * Highlights the Predictive Value of Hexamer Scores in Detecting Alterations of Exonic Splicing Regulatory
     * Elements</i>
     */
    @Test
    void realVariant() {
        final SequenceInterval si = SequenceInterval.builder()
                .interval(new GenomeInterval(rd, Strand.FWD, 1, 0, 125))
                .sequence("cccagGGT" +
                        "C" + // c.520C>T
                        "GTCAGACACCAAAACATATTTCTGAAAGTCTAGGAGCTGAGGTGGATCCTGATATGTCTT" +
                        "G" + // c.581G>A
                        "GTCAAGTTCTTTAGCTACACCACCCACCCTTAGTT" +
                        "C" + // c.617C>G
                        "TACTGTGCTCATAGgtaat")
                .build();
        // representing the c.520C>T variant from Figure 3
        final GenomeVariant first = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 9, PositionType.ONE_BASED), "C", "T");
        double score = calculator.score(first, st, si);
        assertThat(score, is(closeTo(2.811, EPSILON)));

        // representing the c.581G>A variant from Figure 3
        final GenomeVariant second = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 70, PositionType.ONE_BASED), "G", "A");
        score = calculator.score(second, st, si);
        assertThat(score, is(closeTo(3.006, EPSILON)));

        // representing the c.617C>G variant from Figure 3
        final GenomeVariant third = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 106, PositionType.ONE_BASED), "C", "G");
        score = calculator.score(third, st, si);
        assertThat(score, is(closeTo(1.115, EPSILON)));
    }
}