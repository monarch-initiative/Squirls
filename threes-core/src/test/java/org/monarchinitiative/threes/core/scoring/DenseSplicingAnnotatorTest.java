package org.monarchinitiative.threes.core.scoring;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.threes.core.PojosForTesting;
import org.monarchinitiative.threes.core.TestDataSourceConfig;
import org.monarchinitiative.threes.core.classifier.FeatureData;
import org.monarchinitiative.threes.core.data.ic.SplicingPwmData;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.scoring.conservation.BigWigAccessor;
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
class DenseSplicingAnnotatorTest {

    private static final double EPSILON = 0.0005;

    @Autowired
    private ReferenceDictionary referenceDictionary;

    @Autowired
    private SplicingPwmData splicingPwmData;

    @Qualifier("hexamerMap")
    @Autowired
    private Map<String, Double> hexamerMap;

    @Qualifier("septamerMap")
    @Autowired
    private Map<String, Double> septamerMap;

    @Mock
    private BigWigAccessor accessor;

    private SplicingTranscript transcript;

    private SequenceInterval sequenceInterval;

    private DenseSplicingAnnotator evaluator;


    @BeforeEach
    void setUp() {
        transcript = PojosForTesting.getTranscriptWithThreeExons(referenceDictionary);
        sequenceInterval = PojosForTesting.getSequenceIntervalForTranscriptWithThreeExons(referenceDictionary);

        evaluator = new DenseSplicingAnnotator(splicingPwmData, hexamerMap, septamerMap, accessor);
    }

    @Test
    void firstExonDonor() {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1199), "G", "A");

        final FeatureData data = evaluator.evaluate(variant, transcript, sequenceInterval).getFeatureData();

        assertThat(data.getFeature("cryptic_donor", Double.class), is(closeTo(0., EPSILON)));
        assertThat(data.getFeature("canonical_donor", Double.class), is(closeTo(3.0547, EPSILON)));
    }

    @Test
    void secondExonDonor() throws Exception {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1599), "C", "A");

        when(accessor.getScores(variant.getGenomeInterval())).thenReturn(List.of(.12345F));
        final FeatureData data = evaluator.evaluate(variant, transcript, sequenceInterval).getFeatureData();

        assertThat(data.getFeature("cryptic_donor", Double.class), is(closeTo(0., EPSILON)));
        assertThat(data.getFeature("canonical_donor", Double.class), is(closeTo(-1.7926, EPSILON)));
        assertThat(data.getFeature("cryptic_acceptor", Double.class), is(closeTo(-8.1159, EPSILON)));
        assertThat(data.getFeature("canonical_acceptor", Double.class), is(closeTo(0., EPSILON)));

        assertThat(data.getFeature("hexamer", Double.class), is(closeTo(1.306309, EPSILON)));
        assertThat(data.getFeature("septamer", Double.class), is(closeTo(.339600, EPSILON)));

        assertThat(data.getFeature("phylop", Double.class), is(closeTo(.12345, EPSILON)));

    }

    @Test
    void secondExonAcceptor() {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1399), "g", "a");

        final FeatureData data = evaluator.evaluate(variant, transcript, sequenceInterval).getFeatureData();

        assertThat(data.getFeature("canonical_acceptor", Double.class), is(closeTo(9.9600, EPSILON)));
        assertThat(data.getFeature("canonical_donor", Double.class), is(closeTo(0., EPSILON)));
        assertThat(data.getFeature("cryptic_acceptor", Double.class), is(closeTo(6.7992, EPSILON)));
        assertThat(data.getFeature("cryptic_donor", Double.class), is(closeTo(4.7136, EPSILON)));
    }

    @Test
    void thirdExonAcceptor() {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1804), "C", "T");

        final FeatureData data = evaluator.evaluate(variant, transcript, sequenceInterval).getFeatureData();
//        data.getScoresMap().keySet().stream().sorted()
//                .forEach(key -> System.out.println(key + "=" + data.getScoresMap().get(key)));

        assertThat(data.getFeature("canonical_acceptor", Double.class), is(closeTo(0., EPSILON)));
        assertThat(data.getFeature("cryptic_acceptor", Double.class), is(closeTo(-8.9753, EPSILON)));
    }

}