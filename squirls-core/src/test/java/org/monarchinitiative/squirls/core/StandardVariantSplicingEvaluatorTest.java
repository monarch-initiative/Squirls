package org.monarchinitiative.squirls.core;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.data.ReferenceDictionaryBuilder;
import de.charite.compbio.jannovar.reference.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.squirls.core.classifier.SquirlsClassifier;
import org.monarchinitiative.squirls.core.classifier.StandardPrediction;
import org.monarchinitiative.squirls.core.classifier.transform.prediction.IdentityTransformer;
import org.monarchinitiative.squirls.core.data.SplicingAnnotationData;
import org.monarchinitiative.squirls.core.data.SplicingAnnotationDataSource;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.scoring.FloatRegion;
import org.monarchinitiative.squirls.core.scoring.SequenceRegion;
import org.monarchinitiative.squirls.core.scoring.SplicingAnnotator;
import org.monarchinitiative.squirls.core.scoring.TrackRegion;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Here we test some real-world variants.
 */
@SpringBootTest(classes = TestDataSourceConfig.class)
class StandardVariantSplicingEvaluatorTest {

    private static ReferenceDictionary RD;

    @Mock
    private SplicingAnnotationDataSource annotationDataSource;

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
    }

    @BeforeEach
    void setUp() {
        when(annotationDataSource.getReferenceDictionary()).thenReturn(RD);

        evaluator = StandardVariantSplicingEvaluator.builder()
                .annDataSource(annotationDataSource)
                .annotator(annotator)
                .classifier(classifier)
                .transformer(IdentityTransformer.getInstance())
                .build();
    }

    /**
     * This test only specifies variant coordinates, thus it is evaluated with respect to all transcripts it overlaps
     * with. In this case, we evaluate the variant wrt one transcript <code>stx</code>.
     */
    @Test
    void evaluateWrtCoordinates() {
        // arrange
        int begin = 136_223_948, end = 136_223_949;
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(RD, Strand.FWD, 9, end, PositionType.ONE_BASED), "G", "C");

        // 1 - splicing annotation data source
        final SplicingTranscript stx = PojosForTesting.surf2_NM_017503_5(RD);
        final GenomeInterval trackInterval = new GenomeInterval(RD, Strand.FWD, 9, 136_223_945, 136_223_955);
        Map<String, SplicingAnnotationData> annData = Map.of("SURF2",
                new SimpleSplicingAnnotationData(Set.of(stx),
                        Map.of("fasta", SequenceRegion.of(trackInterval, "ACGTacgtAC"),
                                "phylop", FloatRegion.of(trackInterval, List.of(.1f, .1f, .1f, .1f, .1f, .1f, .1f, .1f, .1f, .1f)))));
        final Map<String, ? extends TrackRegion<?>> tracks = annData.get("SURF2").getTracks();

        when(annotationDataSource.getAnnotationData("chr9", begin, end))
                .thenReturn(annData);

        // 2 - splicing annotator
        final SplicingPredictionData plain = StandardSplicingPredictionData.of(variant, stx, tracks);
        final SplicingPredictionData annotated = StandardSplicingPredictionData.of(variant, stx, tracks);
        annotated.putFeature("donor_offset", 5);
        annotated.putFeature("acceptor_offset", 1234); // not real
        when(annotator.annotate(plain)).thenReturn(annotated);

        // 3 - classifier
        final SplicingPredictionData predicted = StandardSplicingPredictionData.of(variant, stx, tracks);
        predicted.putFeature("donor_offset", 5);
        predicted.putFeature("acceptor_offset", 1234); // not real
        predicted.setPrediction(StandardPrediction.builder()
                .addProbaThresholdPair("donor", .6, .7)
                .addProbaThresholdPair("acceptor", .1, .6)
                .build());
        when(classifier.predict(annotated)).thenReturn(predicted);

        // act
        final Map<String, SplicingPredictionData> predictionMap = evaluator.evaluate("chr9", end, "G", "C");

        // assert
        assertThat(predictionMap.size(), is(1));
        assertThat(predictionMap, hasKey("NM_017503.5"));
        assertThat(predictionMap, hasValue(predicted));

        verify(annotator).annotate(plain);
    }

    @Test
    void evaluateWrtTx() {
        // arrange
        int begin = 136_223_948, end = 136_223_949;
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(RD, Strand.FWD, 9, end, PositionType.ONE_BASED), "G", "C");

        // 1 - splicing annotation data source
        final SplicingTranscript stx = PojosForTesting.surf2_NM_017503_5(RD);
        final GenomeInterval trackInterval = new GenomeInterval(RD, Strand.FWD, 9, 136_223_945, 136_223_955);
        Map<String, SplicingAnnotationData> annData = Map.of("SURF2",
                new SimpleSplicingAnnotationData(Set.of(stx),
                        Map.of("fasta", SequenceRegion.of(trackInterval, "ACGTacgtAC"),
                                "phylop", FloatRegion.of(trackInterval, List.of(.1f, .1f, .1f, .1f, .1f, .1f, .1f, .1f, .1f, .1f)))));
        final Map<String, ? extends TrackRegion<?>> tracks = annData.get("SURF2").getTracks();

        when(annotationDataSource.getAnnotationData("chr9", begin, end))
                .thenReturn(annData);


        // 2 - splicing annotator
        final SplicingPredictionData plain = StandardSplicingPredictionData.of(variant, stx, tracks);
        final SplicingPredictionData annotated = StandardSplicingPredictionData.of(variant, stx, tracks);
        annotated.putFeature("donor_offset", 5);
        annotated.putFeature("acceptor_offset", 1234); // not real

        when(annotator.annotate(plain)).thenReturn(annotated);

        // 3 - classifier
        StandardPrediction prediction = StandardPrediction.builder()
                .addProbaThresholdPair("donor", .6, .7)
                .addProbaThresholdPair("acceptor", .1, .6)
                .build();
        final SplicingPredictionData predicted = StandardSplicingPredictionData.of(variant, stx, tracks);
        predicted.putFeature("donor_offset", 5);
        predicted.putFeature("acceptor_offset", 1234); // not real
        predicted.setPrediction(prediction);

        when(classifier.predict(annotated)).thenReturn(predicted);

        // act
        final Map<String, SplicingPredictionData> predictions = evaluator.evaluate("chr9", 136_223_949, "G", "C", Set.of("NM_017503.5"));


        // assert
        assertThat(predictions.size(), is(1));
        assertThat(predictions, hasKey("NM_017503.5"));
        assertThat(predictions, hasValue(predicted));

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
        int begin = 136_223_948, end = 136_223_949;

        final SplicingTranscript stx = PojosForTesting.surf2_NM_017503_5(RD);
        final GenomeInterval trackInterval = new GenomeInterval(RD, Strand.FWD, 9, 136_223_945, 136_223_955);
        Map<String, SplicingAnnotationData> annData = Map.of("SURF2",
                new SimpleSplicingAnnotationData(Set.of(stx),
                        Map.of("fasta", SequenceRegion.of(trackInterval, "ACGTacgtAC"),
                                "phylop", FloatRegion.of(trackInterval, List.of(.1f, .1f, .1f, .1f, .1f, .1f, .1f, .1f, .1f, .1f)))));
        final Map<String, ? extends TrackRegion<?>> tracks = annData.get("SURF2").getTracks();

        when(annotationDataSource.getAnnotationData("chr9", begin, end))
                .thenReturn(annData);

        // act
        final Map<String, SplicingPredictionData> predictionMap = evaluator.evaluate("chr9", end, "G", "C", Set.of("BLABLA"));

        // assert
        assertThat(predictionMap, is(anEmptyMap()));
    }

    @Test
    void evaluateWrtCoordinates_longVariant() {
        // arrange
        int pos = 136_223_949;
        final char[] chars = new char[101]; // default cutoff is 100bp
        Arrays.fill(chars, 'A');
        String ref = new String(chars);
        String alt = "A";

        // act
        final Map<String, SplicingPredictionData> predictionMap = evaluator.evaluate("chr9", pos, ref, alt);

        // assert
        assertThat(predictionMap, is(anEmptyMap()));
    }
}