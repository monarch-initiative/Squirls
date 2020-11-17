package org.monarchinitiative.squirls.core;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.data.ReferenceDictionaryBuilder;
import de.charite.compbio.jannovar.reference.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.squirls.core.classifier.PartialPrediction;
import org.monarchinitiative.squirls.core.classifier.SquirlsClassifier;
import org.monarchinitiative.squirls.core.classifier.StandardPrediction;
import org.monarchinitiative.squirls.core.classifier.transform.prediction.IdentityTransformer;
import org.monarchinitiative.squirls.core.data.SplicingTranscriptSource;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.scoring.SplicingAnnotator;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessor;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;
import xyz.ielis.hyperutil.reference.fasta.SequenceIntervalDefault;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
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
    private SquirlsClassifier classifier;

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
        SI = SequenceIntervalDefault.builder()
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

    @Test
    void evaluateWrtTx() throws Exception {
        // arrange
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(RD, Strand.FWD, 9, 136_223_949, PositionType.ONE_BASED), "G", "C");

        // 0 - splicing transcript source
        final SplicingTranscript stx = PojosForTesting.surf2_NM_017503_5(RD);
        when(transcriptSource.fetchTranscriptByAccession("NM_017503.5", RD))
                .thenReturn(Optional.of(stx));

        // 1 - genome sequence accessor
        when(accessor.fetchSequence(any(GenomeInterval.class))).thenReturn(Optional.of(SI));

        // 2 - splicing annotator
        SplicingPredictionData plain = StandardSplicingPredictionData.of(variant, stx, SI);
        SplicingPredictionData annotated = StandardSplicingPredictionData.of(variant, stx, SI);
        annotated.putFeature("donor_offset", 5);
        annotated.putFeature("acceptor_offset", 1234); // not real
        when(annotator.annotate(plain)).thenReturn(annotated);

        // 3 - classifier
        StandardPrediction prediction = StandardPrediction.of(
                PartialPrediction.of("donor", .6, .7),
                PartialPrediction.of("acceptor", .1, .6));
        SplicingPredictionData predicted = NoRefSplicingPredictionData.of(variant, stx);
        predicted.putFeature("donor_offset", 5);
        predicted.putFeature("acceptor_offset", 1234); // not real
        predicted.setPrediction(prediction);
        when(classifier.predict(annotated)).thenReturn(predicted);

        // act
        final Map<String, SplicingPredictionData> predictions = evaluator.evaluate("chr9", 136_223_949, "G", "C", Set.of("NM_017503.5"));


        // assert
        assertThat(predictions.size(), is(1));
        assertThat(predictions, hasKey("NM_017503.5"));

        SplicingPredictionData actual = predictions.get("NM_017503.5");
        assertThat(actual.getVariant(), is(variant));
        assertThat(actual.getTranscript(), is(stx));
        assertThat(actual.getSequence(), is(SequenceInterval.empty()));
        assertThat(actual.getFeatureAsInt("donor_offset"), is(5));
        assertThat(actual.getFeatureAsInt("acceptor_offset"), is(1234));
        assertThat(actual.getPrediction(), is(prediction));
        assertThat(actual.getMetadata(), is(nullValue()));

        verify(accessor).fetchSequence(new GenomeInterval(RD, Strand.FWD, 9, 136_223_176, 136_228_284, PositionType.ONE_BASED));
        verify(annotator).annotate(plain);
    }

    @Test
    void evaluateWrtTx_unknownContig() {
        // arrange & act
        final Map<String, SplicingPredictionData> predictionMap = evaluator.evaluate("BLA", 100, "G", "C");

        // assert
        assertThat(predictionMap, is(anEmptyMap()));
    }

    @Test
    void evaluateWrtTx_unknownTx() {
        // arrange
        when(transcriptSource.fetchTranscriptByAccession("BLABLA", RD)).thenReturn(Optional.empty());

        // act
        final Map<String, SplicingPredictionData> predictionMap = evaluator.evaluate("chr9", 136_223_949, "G", "C", Set.of("BLABLA"));

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
        final Map<String, SplicingPredictionData> predictionMap = evaluator.evaluate("chr9", 136_223_949, "G", "C", Set.of("NM_017503.5"));

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
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(RD, Strand.FWD, 9, 136_223_949, PositionType.ONE_BASED), "G", "C");

        // 0 - splicing transcript source
        final SplicingTranscript stx = PojosForTesting.surf2_NM_017503_5(RD);
        when(transcriptSource.fetchTranscripts("chr9", 136_223_948, 136_223_949, RD)).thenReturn(List.of(stx));

        // 1 - genome sequence accessor
        when(accessor.fetchSequence(any(GenomeInterval.class))).thenReturn(Optional.of(SI));

        // 2 - splicing annotator
        final SplicingPredictionData plain = StandardSplicingPredictionData.of(variant, stx, SI);
        final SplicingPredictionData annotated = StandardSplicingPredictionData.of(variant, stx, SI);
        annotated.putFeature("donor_offset", 5);
        annotated.putFeature("acceptor_offset", 1234); // not real

        when(annotator.annotate(plain)).thenReturn(annotated);

        // 3 - classifier
        StandardPrediction prediction = StandardPrediction.of(
                PartialPrediction.of("donor", .6, .7),
                PartialPrediction.of("acceptor", .1, .6));
        SplicingPredictionData predicted = StandardSplicingPredictionData.of(variant, stx, SI);
        predicted.putFeature("donor_offset", 5);
        predicted.putFeature("acceptor_offset", 1234); // not real
        predicted.setPrediction(prediction);

        when(classifier.predict(annotated)).thenReturn(predicted);

        // act
        final Map<String, SplicingPredictionData> predictions = evaluator.evaluate("chr9", 136_223_949, "G", "C");

        // assert
        assertThat(predictions.size(), is(1));
        assertThat(predictions, hasKey("NM_017503.5"));

        SplicingPredictionData actual = predictions.get("NM_017503.5");
        assertThat(actual.getVariant(), is(variant));
        assertThat(actual.getTranscript(), is(stx));
        assertThat(actual.getSequence(), is(SequenceInterval.empty()));
        assertThat(actual.getFeatureAsInt("donor_offset"), is(5));
        assertThat(actual.getFeatureAsInt("acceptor_offset"), is(1234));
        assertThat(actual.getPrediction(), is(prediction));
        assertThat(actual.getMetadata(), is(nullValue()));

        verify(accessor).fetchSequence(new GenomeInterval(RD, Strand.FWD, 9, 136_223_176, 136_228_284, PositionType.ONE_BASED));
        verify(annotator).annotate(plain);
    }
}