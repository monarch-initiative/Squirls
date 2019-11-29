package org.monarchinitiative.threes.core.scoring;

import com.google.common.collect.ImmutableMap;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.monarchinitiative.threes.core.PojosForTesting;
import org.monarchinitiative.threes.core.TestDataSourceConfig;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.reference.SplicingLocationData;
import org.monarchinitiative.threes.core.reference.transcript.SplicingTranscriptLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = TestDataSourceConfig.class)
class SimpleSplicingEvaluatorTest {


    private SequenceInterval sequence;
    private SplicingTranscript transcript;


    @Mock
    private ScorerFactory factory;

    @Mock
    private SplicingTranscriptLocator locator;

    private SimpleSplicingEvaluator evaluator;

    @Autowired
    private ReferenceDictionary referenceDictionary;

    @BeforeEach
    void setUp() {
        evaluator = new SimpleSplicingEvaluator(factory, locator);
        sequence = PojosForTesting.getSequenceIntervalForTranscriptWithThreeExons(referenceDictionary);
        transcript = PojosForTesting.getTranscriptWithThreeExons(referenceDictionary);
    }

    @Test
    void variantInFirstDonor() {
        // this variant is in last base of the exon
        final SplicingLocationData ld = SplicingLocationData.newBuilder()
                .setSplicingPosition(SplicingLocationData.SplicingPosition.DONOR)
                .setExonIndex(0)
                .setIntronIndex(0)
                .build();

        when(locator.locate(Mockito.any(GenomeVariant.class), Mockito.any(SplicingTranscript.class))).thenReturn(ld);
        when(factory.scorerForStrategy(ScoringStrategy.CANONICAL_DONOR)).thenReturn(t -> 0.5); // canonical donor outputs 0.5
        when(factory.scorerForStrategy(ScoringStrategy.CRYPTIC_DONOR_IN_CANONICAL_POSITION)).thenReturn(t -> 0.8); // this guy returns 0.8

        // variant at -1 position

        GenomeVariant firstExonDonor = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1199), "C", "T");

        final SplicingPathogenicityData data = evaluator.evaluate(firstExonDonor, transcript, sequence);

        ImmutableMap<ScoringStrategy, Double> scoresMap = data.getScoresMap();
        assertThat(scoresMap.size(), is(2));
        assertThat(scoresMap, hasEntry(ScoringStrategy.CANONICAL_DONOR, 0.5));
        assertThat(scoresMap, hasEntry(ScoringStrategy.CRYPTIC_DONOR_IN_CANONICAL_POSITION, 0.8));
    }


    @Test
    void variantInFirstAcceptor() {
        // this variant affects the 1st base of the 2nd exon
        final SplicingLocationData ld = SplicingLocationData.newBuilder()
                .setSplicingPosition(SplicingLocationData.SplicingPosition.ACCEPTOR)
                .setExonIndex(1)
                .setIntronIndex(0)
                .build();

        when(locator.locate(Mockito.any(GenomeVariant.class), Mockito.any(SplicingTranscript.class))).thenReturn(ld);
        when(factory.scorerForStrategy(ScoringStrategy.CANONICAL_ACCEPTOR)).thenReturn(t -> 0.9); // canonical acceptor outputs 0.9
        when(factory.scorerForStrategy(ScoringStrategy.CRYPTIC_ACCEPTOR_IN_CANONICAL_POSITION)).thenReturn(t -> 0.6); // this guy returns 0.6

        // variant at +1 position
        final GenomeVariant firstExonDonor = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1399), "T", "G");

        final SplicingPathogenicityData data = evaluator.evaluate(firstExonDonor, transcript, sequence);

        ImmutableMap<ScoringStrategy, Double> scoresMap = data.getScoresMap();
        assertThat(scoresMap.size(), is(2));
        assertThat(scoresMap, hasEntry(ScoringStrategy.CANONICAL_ACCEPTOR, 0.9));
        assertThat(scoresMap, hasEntry(ScoringStrategy.CRYPTIC_ACCEPTOR_IN_CANONICAL_POSITION, 0.6));
    }


    @Test
    void variantInTheIntron() {
        // this variant is in the 1st intron, the 3rd base after the donor site
        final SplicingLocationData ld = SplicingLocationData.newBuilder()
                .setSplicingPosition(SplicingLocationData.SplicingPosition.INTRON)
                .setIntronIndex(0)
                .build();

        when(locator.locate(Mockito.any(GenomeVariant.class), Mockito.any(SplicingTranscript.class))).thenReturn(ld);
        when(factory.scorerForStrategy(ScoringStrategy.CRYPTIC_DONOR)).thenReturn(t -> 0.7); // cryptic donor outputs 0.7
        when(factory.scorerForStrategy(ScoringStrategy.CRYPTIC_ACCEPTOR)).thenReturn(t -> 0.5); // cryptic acceptor returns 0.5

        // variant 3bp downstream from the end of the 1st donor site (in intron)
        GenomeVariant firstExonDonor = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1208), "A", "G");

        final SplicingPathogenicityData data = evaluator.evaluate(firstExonDonor, transcript, sequence);

        ImmutableMap<ScoringStrategy, Double> scoresMap = data.getScoresMap();
        assertThat(scoresMap.size(), is(2));
        assertThat(scoresMap, hasEntry(ScoringStrategy.CRYPTIC_DONOR, 0.7));
        assertThat(scoresMap, hasEntry(ScoringStrategy.CRYPTIC_ACCEPTOR, 0.5));
    }

    // TODO - improve coverage
}