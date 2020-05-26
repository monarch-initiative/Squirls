package org.monarchinitiative.threes.core;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.data.ReferenceDictionaryBuilder;
import de.charite.compbio.jannovar.reference.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.threes.core.classifier.FeatureData;
import org.monarchinitiative.threes.core.classifier.OverlordClassifier;
import org.monarchinitiative.threes.core.classifier.PredictionImpl;
import org.monarchinitiative.threes.core.data.SplicingTranscriptSource;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.scoring.SplicingAnnotator;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessor;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Here we test some real-world variants.
 */
@SpringBootTest(classes = TestDataSourceConfig.class)
class VariantSplicingEvaluatorTest {

    /**
     * FASTA file containing 20kb region at chr9 that spans my favourite genes <em>SURF1</em> and <em>SURF2</em>
     * chr9:136210000-136230000
     */
    private static final Path SURF_FASTA_PATH = Paths.get(VariantSplicingEvaluatorTest.class.getResource("hg19_small_surf.fa").getPath());


    private static SequenceInterval SI;

    private static ReferenceDictionary RD;

    @Mock
    private GenomeSequenceAccessor accessor;

    @Mock
    private SplicingTranscriptSource transcriptSource;

    @Mock
    private SplicingAnnotator annotator;

    @Mock
    private OverlordClassifier classifier;


    private VariantSplicingEvaluator evaluator;


    @BeforeAll
    static void beforeAll() throws Exception {
        final String fastaSeq;
        try (final BufferedReader reader = Files.newBufferedReader(SURF_FASTA_PATH)) {
            fastaSeq = reader.lines()
                    .filter(line -> !line.isBlank() && !line.startsWith(">")) //  remove header or empty lines
                    .collect(Collectors.joining(""));
        }

        final ReferenceDictionaryBuilder rdBuilder = new ReferenceDictionaryBuilder();
        rdBuilder.putContigID("chr9", 9);
        rdBuilder.putContigID("9", 9);
        rdBuilder.putContigName(9, "chr9");
        rdBuilder.putContigLength(9, 141_213_431);
        RD = rdBuilder.build();
        SI = SequenceInterval.builder()
                .interval(new GenomeInterval(RD, Strand.FWD, 9, 136210000, 136230000, PositionType.ONE_BASED))
                .sequence(fastaSeq)
                .build();
    }

    @BeforeEach
    void setUp() {
        // genome sequence accessor
        when(accessor.getReferenceDictionary()).thenReturn(RD);
        evaluator = new VariantSplicingEvaluatorImpl(accessor, transcriptSource, annotator, classifier);
    }

    @Test
    void evaluateWrtTx() throws Exception {
        // arrange
        // 0 - splicing transcript source
        final SplicingTranscript stx = PojosForTesting.surf2_NM_017503_5(RD);
        when(transcriptSource.fetchTranscriptByAccession("NM_017503.5", RD))
                .thenReturn(Optional.of(stx));

        // 1 - genome sequence accessor
        when(accessor.fetchSequence(any(GenomeInterval.class))).thenReturn(Optional.of(SI));

        // 2 - splicing annotator
        final FeatureData featureData = FeatureData.builder()
                // 'donor_offset', 'canonical_donor', 'cryptic_donor',
                // 'acceptor_offset', 'canonical_acceptor', 'cryptic_acceptor'
                // 'phylop', 'hexamer', 'septamer'
                .addFeature("donor_offset", 5)
                .addFeature("acceptor_offset", 1234) // not real
                .build();
        when(annotator.evaluate(any(GenomeVariant.class), eq(stx), eq(SI))).thenReturn(featureData);

        // 3 - classifier
        Prediction prediction = PredictionImpl.builder()
                .setDonorData(.6, .7)
                .setAcceptorData(.1, .6)
                .build();
        when(classifier.predict(featureData)).thenReturn(prediction);

        // act
        final Map<String, Prediction> predictionMap = evaluator.evaluate("chr9", 136_223_949, "G", "C", Set.of("NM_017503.5"));

        // assert
        assertThat(predictionMap, hasKey("NM_017503.5"));
        assertThat(predictionMap, hasValue(prediction));
        assertThat(predictionMap.size(), is(1));

        verify(accessor).fetchSequence(new GenomeInterval(RD, Strand.FWD, 9, 136_223_326, 136_228_134, PositionType.ONE_BASED));
        verify(annotator).evaluate(new GenomeVariant(new GenomePosition(RD, Strand.FWD, 9, 136_223_949, PositionType.ONE_BASED), "G", "C"), stx, SI);
    }

    @Test
    void evaluateWrtTx_unknownContig() {
        // arrange & act
        final Map<String, Prediction> predictionMap = evaluator.evaluate("BLA", 100, "G", "C");

        // assert
        assertThat(predictionMap, is(anEmptyMap()));
    }

    @Test
    void evaluateWrtTx_unknownTx() {
        // arrange
        when(transcriptSource.fetchTranscriptByAccession("BLABLA", RD)).thenReturn(Optional.empty());

        // act
        final Map<String, Prediction> predictionMap = evaluator.evaluate("chr9", 136_223_949, "G", "C", Set.of("BLABLA"));

        // assert
        assertThat(predictionMap, is(anEmptyMap()));
    }

    @Test
    void evaluateWrtTx_notEnoughSequenceAvailable() {
        // arrange
        // 0 - splicing transcript source
        final SplicingTranscript stx = PojosForTesting.surf2_NM_017503_5(RD);
        when(transcriptSource.fetchTranscriptByAccession("NM_017503.5", RD)).thenReturn(Optional.of(stx));

        // 1 - genome sequence accessor
        when(accessor.fetchSequence(any(GenomeInterval.class))).thenReturn(Optional.empty());

        // act
        final Map<String, Prediction> predictionMap = evaluator.evaluate("chr9", 136_223_949, "G", "C", Set.of("NM_017503.5"));

        // assert
        assertThat(predictionMap, is(anEmptyMap()));
    }


    /**
     * This test only specifies variant coordinates, thus it is evaluated with respect to all transcripts it overlaps
     * with. In this case, we evaluate the variant wrt one transcript <code>stx</code>.
     */
    @Test
    void evaluateWrtCoordinates() throws Exception {
        // arrange
        // 0 - splicing transcript source
        final SplicingTranscript stx = PojosForTesting.surf2_NM_017503_5(RD);
        when(transcriptSource.fetchTranscripts("chr9", 136223948, 136223949, RD)).thenReturn(List.of(stx));

        // 1 - genome sequence accessor
        when(accessor.fetchSequence(any(GenomeInterval.class))).thenReturn(Optional.of(SI));

        // 2 - splicing annotator
        final FeatureData featureData = FeatureData.builder()
                // 'donor_offset', 'canonical_donor', 'cryptic_donor',
                // 'acceptor_offset', 'canonical_acceptor', 'cryptic_acceptor'
                // 'phylop', 'hexamer', 'septamer'
                .addFeature("donor_offset", 5) // real, but not required
                .addFeature("acceptor_offset", 1234) // not real
                .build();
        when(annotator.evaluate(any(GenomeVariant.class), eq(stx), eq(SI))).thenReturn(featureData);

        // 3 - classifier
        Prediction prediction = PredictionImpl.builder()
                .setDonorData(.6, .7)
                .setAcceptorData(.1, .6)
                .build();
        when(classifier.predict(featureData)).thenReturn(prediction);

        // act
        final Map<String, Prediction> predictionMap = evaluator.evaluate("chr9", 136_223_949, "G", "C");

        // assert
        assertThat(predictionMap, hasKey("NM_017503.5"));
        assertThat(predictionMap, hasValue(prediction));
        assertThat(predictionMap.size(), is(1));

        verify(accessor).fetchSequence(new GenomeInterval(RD, Strand.FWD, 9, 136_223_326, 136_228_134, PositionType.ONE_BASED));
        verify(annotator).evaluate(new GenomeVariant(new GenomePosition(RD, Strand.FWD, 9, 136_223_949, PositionType.ONE_BASED), "G", "C"), stx, SI);
    }


}