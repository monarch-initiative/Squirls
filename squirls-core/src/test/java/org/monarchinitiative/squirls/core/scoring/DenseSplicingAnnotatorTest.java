package org.monarchinitiative.squirls.core.scoring;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.squirls.core.PojosForTesting;
import org.monarchinitiative.squirls.core.SimpleAnnotatable;
import org.monarchinitiative.squirls.core.TestDataSourceConfig;
import org.monarchinitiative.squirls.core.data.ic.SplicingPwmData;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.scoring.conservation.BigWigAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = TestDataSourceConfig.class)
public class DenseSplicingAnnotatorTest {

    public static final double EPSILON = 0.0005;

    @Autowired
    public ReferenceDictionary referenceDictionary;

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

    public SplicingTranscript transcript;

    public SequenceInterval sequenceInterval;

    public DenseSplicingAnnotator evaluator;


    @BeforeEach
    public void setUp() {
        transcript = PojosForTesting.getTranscriptWithThreeExons(referenceDictionary);
        sequenceInterval = PojosForTesting.getSequenceIntervalForTranscriptWithThreeExons(referenceDictionary);
        evaluator = new DenseSplicingAnnotator(splicingPwmData, hexamerMap, septamerMap, accessor);
    }

    @Test
    public void firstExonDonor() {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1199), "G", "A");

        SimpleAnnotatable ann = new SimpleAnnotatable(variant, transcript, sequenceInterval);
        ann = evaluator.annotate(ann);

        assertThat(ann.getFeature("cryptic_donor", Double.class), is(closeTo(0., EPSILON)));
        assertThat(ann.getFeature("canonical_donor", Double.class), is(closeTo(3.0547, EPSILON)));
    }

    @Test
    public void secondExonDonor() throws Exception {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1599), "C", "A");
        when(accessor.getScores(variant.getGenomeInterval())).thenReturn(List.of(.12345F));

        SimpleAnnotatable ann = new SimpleAnnotatable(variant, transcript, sequenceInterval);
        ann = evaluator.annotate(ann);

        assertThat(ann.getFeature("cryptic_donor", Double.class), is(closeTo(0., EPSILON)));
        assertThat(ann.getFeature("canonical_donor", Double.class), is(closeTo(-1.7926, EPSILON)));
        assertThat(ann.getFeature("cryptic_acceptor", Double.class), is(closeTo(-8.1159, EPSILON)));
        assertThat(ann.getFeature("canonical_acceptor", Double.class), is(closeTo(0., EPSILON)));

        assertThat(ann.getFeature("hexamer", Double.class), is(closeTo(1.306309, EPSILON)));
        assertThat(ann.getFeature("septamer", Double.class), is(closeTo(.339600, EPSILON)));

        assertThat(ann.getFeature("phylop", Double.class), is(closeTo(.12345, EPSILON)));

    }

    @Test
    public void secondExonAcceptor() {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1399), "g", "a");
        SimpleAnnotatable ann = new SimpleAnnotatable(variant, transcript, sequenceInterval);
        ann = evaluator.annotate(ann);

        assertThat(ann.getFeature("canonical_acceptor", Double.class), is(closeTo(9.9600, EPSILON)));
        assertThat(ann.getFeature("canonical_donor", Double.class), is(closeTo(0., EPSILON)));
        assertThat(ann.getFeature("cryptic_acceptor", Double.class), is(closeTo(6.7992, EPSILON)));
        assertThat(ann.getFeature("cryptic_donor", Double.class), is(closeTo(4.7136, EPSILON)));
    }

    @Test
    public void thirdExonAcceptor() {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1804), "C", "T");
        SimpleAnnotatable ann = new SimpleAnnotatable(variant, transcript, sequenceInterval);
        ann = evaluator.annotate(ann);

        assertThat(ann.getFeature("canonical_acceptor", Double.class), is(closeTo(0., EPSILON)));
        assertThat(ann.getFeature("cryptic_acceptor", Double.class), is(closeTo(-8.9753, EPSILON)));
    }

}