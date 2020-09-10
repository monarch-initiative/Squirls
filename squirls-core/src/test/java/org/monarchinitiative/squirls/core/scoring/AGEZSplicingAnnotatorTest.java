package org.monarchinitiative.squirls.core.scoring;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.monarchinitiative.squirls.core.PojosForTesting;
import org.monarchinitiative.squirls.core.SimpleAnnotatable;
import org.monarchinitiative.squirls.core.TestDataSourceConfig;
import org.monarchinitiative.squirls.core.data.ic.SplicingPwmData;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.scoring.calculators.conservation.BigWigAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasItems;

@SpringBootTest(classes = TestDataSourceConfig.class)
public class AGEZSplicingAnnotatorTest {

    private static final double EPSILON = 0.0005;

    @Autowired
    public ReferenceDictionary rd;

    @Autowired
    public SplicingPwmData splicingPwmData;

    @Qualifier("hexamerMap")
    @Autowired
    public Map<String, Double> hexamerMap;

    @Qualifier("septamerMap")
    @Autowired
    public Map<String, Double> septamerMap;

    @Mock
    public BigWigAccessor accessor;

    private SplicingTranscript st;

    private SequenceInterval sequence;

    private AGEZSplicingAnnotator annotator;


    @BeforeEach
    public void setUp() {
        st = PojosForTesting.getTranscriptWithThreeExons(rd);
        sequence = PojosForTesting.getSequenceIntervalForTranscriptWithThreeExons(rd);
        annotator = new AGEZSplicingAnnotator(splicingPwmData, hexamerMap, septamerMap, accessor);
    }

    @ParameterizedTest
    @CsvSource({
            "1389,c,cag,1.,0.,0.,1.", // match, turns "c" -> "cag" within AGEZ
            "1384,gcc,gaa,0.,1.,0.,0.", // match, "gcc" -> "gaa" deletes 2 cytosines within AGEZ
            "1397,c,a,0.,0.,1.,0.", // "c" -> "a" converts Y to R at -3 position
    })
    public void annotate(int pos, String ref, String alt,
                         double createsAgInAgez, double pptIsTruncated,
                         double purineAtMinusThree, double createsYagInAgez) {

        final GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, pos), ref, alt);

        SimpleAnnotatable ann = new SimpleAnnotatable(variant, st, sequence);
        ann = annotator.annotate(ann);

        assertThat(ann.getFeature("creates_ag_in_agez", Double.class), is(closeTo(createsAgInAgez, EPSILON)));
        assertThat(ann.getFeature("ppt_is_truncated", Double.class), is(closeTo(pptIsTruncated, EPSILON)));
        assertThat(ann.getFeature("yag_at_acceptor_minus_three", Double.class), is(closeTo(purineAtMinusThree, EPSILON)));
        assertThat(ann.getFeature("creates_yag_in_agez", Double.class), is(closeTo(createsYagInAgez, EPSILON)));
    }

    @Test
    public void allFeaturesAreCalculated() {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1389), "c", "cag");

        SimpleAnnotatable ann = new SimpleAnnotatable(variant, st, sequence);
        ann = annotator.annotate(ann);

        // we expect the agez calculator to compute the following features:
        assertThat(ann.getFeatureNames(), hasItems("creates_ag_in_agez", "ppt_is_truncated", "yag_at_acceptor_minus_three", "creates_yag_in_agez"));
    }
}