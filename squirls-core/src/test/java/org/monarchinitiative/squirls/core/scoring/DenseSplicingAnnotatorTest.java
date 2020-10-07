package org.monarchinitiative.squirls.core.scoring;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class DenseSplicingAnnotatorTest extends SplicingAnnotatorTestBase {


    private DenseSplicingAnnotator annotator;


    @BeforeEach
    public void setUp() {
        super.setUp();
        annotator = new DenseSplicingAnnotator(splicingPwmData, hexamerMap, septamerMap);
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
        assertThat(ann.getFeature("cryptic_acceptor", Double.class), is(closeTo(-8.1159, EPSILON)));
        assertThat(ann.getFeature("canonical_acceptor", Double.class), is(closeTo(0., EPSILON)));

        assertThat(ann.getFeature("hexamer", Double.class), is(closeTo(-1.306309, EPSILON)));
        assertThat(ann.getFeature("septamer", Double.class), is(closeTo(-.339600, EPSILON)));

        assertThat(ann.getFeature("phylop", Double.class), is(closeTo(0.647115, EPSILON)));
    }

    @Test
    public void secondExonAcceptor() {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1399), "g", "a");
        Annotatable ann = makeAnnotatable(variant);
        ann = annotator.annotate(ann);

        assertThat(ann.getFeature("canonical_acceptor", Double.class), is(closeTo(9.9600, EPSILON)));
        assertThat(ann.getFeature("canonical_donor", Double.class), is(closeTo(0., EPSILON)));
        assertThat(ann.getFeature("cryptic_acceptor", Double.class), is(closeTo(6.7992, EPSILON)));
        assertThat(ann.getFeature("cryptic_donor", Double.class), is(closeTo(4.7136, EPSILON)));
    }

    @Test
    public void thirdExonAcceptor() {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1804), "C", "T");
        Annotatable ann = makeAnnotatable(variant);
        ann = annotator.annotate(ann);

        assertThat(ann.getFeature("canonical_acceptor", Double.class), is(closeTo(0., EPSILON)));
        assertThat(ann.getFeature("cryptic_acceptor", Double.class), is(closeTo(-8.9753, EPSILON)));
    }

}