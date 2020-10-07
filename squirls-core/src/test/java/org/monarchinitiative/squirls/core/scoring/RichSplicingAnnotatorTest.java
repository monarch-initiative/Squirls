package org.monarchinitiative.squirls.core.scoring;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class RichSplicingAnnotatorTest extends SplicingAnnotatorTestBase {

    private RichSplicingAnnotator annotator;

    @BeforeEach
    public void setUp() {
        super.setUp();
        annotator = new RichSplicingAnnotator(splicingPwmData, hexamerMap, septamerMap);
    }

    @Test
    public void firstExonDonor() {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1199), "G", "A");

        Annotatable ann = makeAnnotatable(variant);
        ann = annotator.annotate(ann);

        assertThat(ann.getFeature("cryptic_donor", Double.class), is(closeTo(0., EPSILON)));
        assertThat(ann.getFeature("canonical_donor", Double.class), is(closeTo(3.0547, EPSILON)));
    }


    @Test
    public void secondExonDonor() throws Exception {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1599), "C", "A");

        Annotatable ann = makeAnnotatable(variant);
        ann = annotator.annotate(ann);

        assertThat(ann.getFeature("cryptic_donor", Double.class), is(closeTo(0., EPSILON)));
        assertThat(ann.getFeature("canonical_donor", Double.class), is(closeTo(-1.7926, EPSILON)));
        assertThat(ann.getFeature("donor_offset", Double.class), is(closeTo(-1., EPSILON)));
        assertThat(ann.getFeature("cryptic_acceptor", Double.class), is(closeTo(-8.1159, EPSILON)));
        assertThat(ann.getFeature("canonical_acceptor", Double.class), is(closeTo(0., EPSILON)));
        assertThat(ann.getFeature("acceptor_offset", Double.class), is(closeTo(200., EPSILON)));

        assertThat(ann.getFeature("hexamer", Double.class), is(closeTo(-1.306309, EPSILON)));
        assertThat(ann.getFeature("septamer", Double.class), is(closeTo(-.339600, EPSILON)));

        // this value is based from random generator in PojosForTesting
        assertThat(ann.getFeature("phylop", Double.class), is(closeTo(.647115, EPSILON)));

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