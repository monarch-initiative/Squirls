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
import org.monarchinitiative.threes.core.classifier.Prediction;
import org.monarchinitiative.threes.core.classifier.StandardPrediction;
import org.monarchinitiative.threes.core.classifier.transform.prediction.IdentityTransformer;
import org.monarchinitiative.threes.core.data.SplicingTranscriptSource;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.scoring.SplicingAnnotationData;
import org.monarchinitiative.threes.core.scoring.SplicingAnnotator;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessor;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.*;

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
class StandardVariantSplicingEvaluatorTest {

    /**
     * Tolerance for numeric comparisons.
     */
    private static final double EPSILON = 5E-6;

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

    private StandardVariantSplicingEvaluator evaluator;


    @BeforeAll
    static void beforeAll() {
        final ReferenceDictionaryBuilder rdBuilder = new ReferenceDictionaryBuilder();
        rdBuilder.putContigID("chr9", 9);
        rdBuilder.putContigID("9", 9);
        rdBuilder.putContigName(9, "chr9");
        rdBuilder.putContigLength(9, 141_213_431);
        RD = rdBuilder.build();
        char[] chars = new char[136_230_000 - 136_210_000 + 1];
        Arrays.fill(chars, 'A'); // the sequence does not really matter since we use mocks
        SI = SequenceInterval.builder()
                .interval(new GenomeInterval(RD, Strand.FWD, 9, 136_210_000, 136_230_000, PositionType.ONE_BASED))
                .sequence(new String(chars))
                .build();
    }

    @BeforeEach
    void setUp() {
        // genome sequence accessor
        when(accessor.getReferenceDictionary()).thenReturn(RD);
        evaluator = StandardVariantSplicingEvaluator.builder()
                .accessor(accessor)
                .txSource(transcriptSource)
                .annotator(annotator)
                .classifier(classifier)
                .transformer(IdentityTransformer.getInstance())
                .build();
    }

    static SplicingAnnotationData toSplicingAnnotationData(FeatureData data) {
        return SplicingAnnotationData.newBuilder().featureData(data).build();
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
        when(annotator.evaluate(any(GenomeVariant.class), eq(stx), eq(SI))).thenReturn(toSplicingAnnotationData(featureData));

        // 3 - classifier
        StandardPrediction prediction = StandardPrediction.builder()
                .addProbaThresholdPair(.6, .7)
                .addProbaThresholdPair(.1, .6)
                .build();
        when(classifier.predict(featureData)).thenReturn(prediction);

        // act
        final Map<String, Prediction> predictionMap = evaluator.evaluate("chr9", 136_223_949, "G", "C", Set.of("NM_017503.5")).getPredictions();

        // assert
        assertThat(predictionMap, hasKey("NM_017503.5"));
        assertThat(predictionMap, hasValue(prediction));
        assertThat(predictionMap.size(), is(1));

        verify(accessor).fetchSequence(new GenomeInterval(RD, Strand.FWD, 9, 136_223_176, 136_228_284, PositionType.ONE_BASED));
        verify(annotator).evaluate(new GenomeVariant(new GenomePosition(RD, Strand.FWD, 9, 136_223_949, PositionType.ONE_BASED), "G", "C"), stx, SI);
    }

    @Test
    void evaluateWrtTx_unknownContig() {
        // arrange & act
        final Map<String, Prediction> predictionMap = evaluator.evaluate("BLA", 100, "G", "C").getPredictions();

        // assert
        assertThat(predictionMap, is(anEmptyMap()));
    }

    @Test
    void evaluateWrtTx_unknownTx() {
        // arrange
        when(transcriptSource.fetchTranscriptByAccession("BLABLA", RD)).thenReturn(Optional.empty());

        // act
        final Map<String, Prediction> predictionMap = evaluator.evaluate("chr9", 136_223_949, "G", "C", Set.of("BLABLA")).getPredictions();

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
        final Map<String, Prediction> predictionMap = evaluator.evaluate("chr9", 136_223_949, "G", "C", Set.of("NM_017503.5")).getPredictions();

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
        when(annotator.evaluate(any(GenomeVariant.class), eq(stx), eq(SI))).thenReturn(toSplicingAnnotationData(featureData));

        // 3 - classifier
        StandardPrediction prediction = StandardPrediction.builder()
                .addProbaThresholdPair(.6, .7)
                .addProbaThresholdPair(.1, .6)
                .build();
        when(classifier.predict(featureData)).thenReturn(prediction);

        // act
        final Map<String, Prediction> predictionMap = evaluator.evaluate("chr9", 136_223_949, "G", "C").getPredictions();

        // assert
        assertThat(predictionMap, hasKey("NM_017503.5"));
        assertThat(predictionMap, hasValue(prediction));
        assertThat(predictionMap.size(), is(1));

        verify(accessor).fetchSequence(new GenomeInterval(RD, Strand.FWD, 9, 136_223_176, 136_228_284, PositionType.ONE_BASED));
        verify(annotator).evaluate(new GenomeVariant(new GenomePosition(RD, Strand.FWD, 9, 136_223_949, PositionType.ONE_BASED), "G", "C"), stx, SI);
    }
}