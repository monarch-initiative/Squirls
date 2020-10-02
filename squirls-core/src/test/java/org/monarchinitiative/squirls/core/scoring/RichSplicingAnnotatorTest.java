package org.monarchinitiative.squirls.core.scoring;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.PojosForTesting;
import org.monarchinitiative.squirls.core.SimpleAnnotatable;
import org.monarchinitiative.squirls.core.TestDataSourceConfig;
import org.monarchinitiative.squirls.core.data.ic.SplicingPwmData;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

@SpringBootTest(classes = TestDataSourceConfig.class)
class RichSplicingAnnotatorTest {

    private static final double EPSILON = 0.0005;

    @Autowired
    private ReferenceDictionary rd;

    @Autowired
    private SplicingPwmData splicingPwmData;

    @Qualifier("hexamerMap")
    @Autowired
    private Map<String, Double> hexamerMap;

    @Qualifier("septamerMap")
    @Autowired
    private Map<String, Double> septamerMap;

    private SplicingTranscript st;

    private SequenceInterval sequence;

    private RichSplicingAnnotator annotator;

    @BeforeEach
    void setUp() {
        st = PojosForTesting.getTranscriptWithThreeExons(rd);
        sequence = PojosForTesting.getSequenceIntervalForTranscriptWithThreeExons(rd);
        annotator = new RichSplicingAnnotator(splicingPwmData, hexamerMap, septamerMap);
    }

    @Test
    void firstExonDonor() {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1199), "G", "A");

        SimpleAnnotatable ann = new SimpleAnnotatable(variant, st, sequence);
        ann = annotator.annotate(ann);

        assertThat(ann.getFeature("cryptic_donor", Double.class), is(closeTo(0., EPSILON)));
        assertThat(ann.getFeature("canonical_donor", Double.class), is(closeTo(3.0547, EPSILON)));
    }


    @Test
    void secondExonDonor() throws Exception {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1599), "C", "A");
//        when(accessor.getScores(variant.getGenomeInterval())).thenReturn(List.of(.12345F));

        final Annotatable ann = annotator.annotate(new SimpleAnnotatable(variant, st, sequence));

        assertThat(ann.getFeature("cryptic_donor", Double.class), is(closeTo(0., EPSILON)));
        assertThat(ann.getFeature("canonical_donor", Double.class), is(closeTo(-1.7926, EPSILON)));
        assertThat(ann.getFeature("donor_offset", Double.class), is(closeTo(-1., EPSILON)));
        assertThat(ann.getFeature("cryptic_acceptor", Double.class), is(closeTo(-8.1159, EPSILON)));
        assertThat(ann.getFeature("canonical_acceptor", Double.class), is(closeTo(0., EPSILON)));
        assertThat(ann.getFeature("acceptor_offset", Double.class), is(closeTo(200., EPSILON)));

        assertThat(ann.getFeature("hexamer", Double.class), is(closeTo(-1.306309, EPSILON)));
        assertThat(ann.getFeature("septamer", Double.class), is(closeTo(-.339600, EPSILON)));
        // TODO: 10/2/20 evaluate
//        assertThat(ann.getFeature("phylop", Double.class), is(closeTo(.12345, EPSILON)));

        assertThat(ann.getFeature("wt_ri_donor", Double.class), is(closeTo(2.8938, EPSILON)));
        assertThat(ann.getFeature("wt_ri_acceptor", Double.class), is(closeTo(4.1148, EPSILON)));
        assertThat(ann.getFeature("alt_ri_best_window_donor", Double.class), is(closeTo(4.6864, EPSILON)));
        assertThat(ann.getFeature("alt_ri_best_window_acceptor", Double.class), is(closeTo(-4.0011, EPSILON)));
        assertThat(ann.getFeature("s_strength_diff_donor", Double.class), is(closeTo(0., EPSILON)));
        assertThat(ann.getFeature("s_strength_diff_acceptor", Double.class), is(closeTo(0., EPSILON)));
        assertThat(ann.getFeature("exon_length", Double.class), is(closeTo(200., EPSILON)));
        assertThat(ann.getFeature("intron_length", Double.class), is(closeTo(200., EPSILON)));
    }
}