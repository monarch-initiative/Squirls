package org.monarchinitiative.threes.core.scoring.dense;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.PojosForTesting;
import org.monarchinitiative.threes.core.TestDataSourceConfig;
import org.monarchinitiative.threes.core.data.ic.SplicingPwmData;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.scoring.SplicingPathogenicityData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

@SpringBootTest(classes = TestDataSourceConfig.class)
class DenseSplicingEvaluatorTest {

    private static final double EPSILON = 0.0005;

    @Autowired
    private ReferenceDictionary referenceDictionary;

    @Autowired
    private SplicingPwmData splicingPwmData;

    private SplicingTranscript transcript;

    private SequenceInterval sequenceInterval;

    private DenseSplicingEvaluator evaluator;


    @BeforeEach
    void setUp() {
        transcript = PojosForTesting.getTranscriptWithThreeExons(referenceDictionary);
        sequenceInterval = PojosForTesting.getSequenceIntervalForTranscriptWithThreeExons(referenceDictionary);

        evaluator = new DenseSplicingEvaluator(splicingPwmData);
    }

    @Test
    void firstExonDonor() {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1199), "G", "A");

        final SplicingPathogenicityData data = evaluator.evaluate(variant, transcript, sequenceInterval);

        assertThat(data.getScoresMap().get("cryptic_donor"), is(closeTo(0., EPSILON)));
        assertThat(data.getScoresMap().get("canonical_donor"), is(closeTo(3.0547, EPSILON)));
    }

    @Test
    void secondExonDonor() {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1599), "C", "A");

        final SplicingPathogenicityData data = evaluator.evaluate(variant, transcript, sequenceInterval);

        assertThat(data.getScoresMap().get("cryptic_donor"), is(closeTo(0., EPSILON)));
        assertThat(data.getScoresMap().get("canonical_donor"), is(closeTo(-1.7926, EPSILON)));
        assertThat(data.getScoresMap().get("cryptic_acceptor"), is(closeTo(-8.1159, EPSILON)));
        assertThat(data.getScoresMap().get("canonical_acceptor"), is(closeTo(0., EPSILON)));
    }

    @Test
    void secondExonAcceptor() {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1399), "g", "a");

        final SplicingPathogenicityData data = evaluator.evaluate(variant, transcript, sequenceInterval);

        assertThat(data.getScoresMap().get("canonical_acceptor"), is(closeTo(9.9600, EPSILON)));
        assertThat(data.getScoresMap().get("canonical_donor"), is(closeTo(0., EPSILON)));
        assertThat(data.getScoresMap().get("cryptic_acceptor"), is(closeTo(6.7992, EPSILON)));
        assertThat(data.getScoresMap().get("cryptic_donor"), is(closeTo(4.7136, EPSILON)));
    }

    @Test
    void thirdExonAcceptor() {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1804), "C", "T");

        final SplicingPathogenicityData data = evaluator.evaluate(variant, transcript, sequenceInterval);
//        data.getScoresMap().keySet().stream().sorted()
//                .forEach(key -> System.out.println(key + "=" + data.getScoresMap().get(key)));

        assertThat(data.getScoresMap().get("canonical_acceptor"), is(closeTo(0., EPSILON)));
        assertThat(data.getScoresMap().get("cryptic_acceptor"), is(closeTo(-8.9753, EPSILON)));
    }

}