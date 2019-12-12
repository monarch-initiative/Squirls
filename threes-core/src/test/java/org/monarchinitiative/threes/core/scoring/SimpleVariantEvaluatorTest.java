package org.monarchinitiative.threes.core.scoring;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.threes.core.PojosForTesting;
import org.monarchinitiative.threes.core.TestDataSourceConfig;
import org.monarchinitiative.threes.core.data.SplicingTranscriptSource;
import org.monarchinitiative.threes.core.scoring.dense.DenseSplicingAnnotator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = TestDataSourceConfig.class)
class SimpleVariantEvaluatorTest {

    @Mock
    private GenomeSequenceAccessor accessor;

    @Mock
    private SplicingTranscriptSource transcriptSource;

    @Autowired
    private DenseSplicingAnnotator denseSplicingEvaluator;

    @Autowired
    private ReferenceDictionary referenceDictionary;

    private SimpleVariantSplicingEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new SimpleVariantSplicingEvaluator(accessor, transcriptSource, denseSplicingEvaluator);
    }

    @Test
    void evaluatePositionData() {
        // arrange mocks
        when(accessor.getReferenceDictionary())
                .thenReturn(referenceDictionary);
        when(accessor.fetchSequence(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 950, 2050)))
                .thenReturn(Optional.of(PojosForTesting.getSequenceIntervalForTranscriptWithThreeExons(referenceDictionary)));

        when(transcriptSource.fetchTranscripts("chr1", 1199, 1200, referenceDictionary))
                .thenReturn(List.of(PojosForTesting.getTranscriptWithThreeExons(referenceDictionary)));

        // act
        Map<String, SplicingPathogenicityData> data = evaluator.evaluate("chr1", 1_200, "G", "A");

        // assert
        assertThat(data.size(), is(1));
        assertThat(data.keySet(), hasItem("FIRST"));
        assertThat(data.values(), hasItem(SplicingPathogenicityData.builder()
                .putScore("cryptic_donor", 0.)
                .putScore("canonical_donor", 3.0547231863408397)
                .build()));

    }

    @Test
    void evaluateVariant() {
        // arrange mocks
        when(accessor.getReferenceDictionary())
                .thenReturn(referenceDictionary);
        when(accessor.fetchSequence(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 950, 2050)))
                .thenReturn(Optional.of(PojosForTesting.getSequenceIntervalForTranscriptWithThreeExons(referenceDictionary)));

        when(transcriptSource.fetchTranscripts("chr1", 1199, 1200, referenceDictionary))
                .thenReturn(List.of(PojosForTesting.getTranscriptWithThreeExons(referenceDictionary)));

        // act
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1199), "G", "A");
        final Map<String, SplicingPathogenicityData> data = evaluator.evaluate(variant);

        // assert
        assertThat(data.size(), is(1));
        assertThat(data.keySet(), hasItem("FIRST"));
        assertThat(data.values(), hasItem(SplicingPathogenicityData.builder()
                .putScore("cryptic_donor", 0.)
                .putScore("canonical_donor", 3.0547231863408397)
                .build()));
    }

    @Test
    void evaluateVariantWhenNoTranscriptsAreAvailable() {
        // arrange mocks
        when(accessor.getReferenceDictionary())
                .thenReturn(referenceDictionary);
        // absent sequence interval
        when(accessor.fetchSequence(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 950, 2050)))
                .thenReturn(Optional.empty());

        when(transcriptSource.fetchTranscripts("chr1", 1199, 1200, referenceDictionary))
                .thenReturn(List.of(PojosForTesting.getTranscriptWithThreeExons(referenceDictionary)));

        // act
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1199), "G", "A");
        final Map<String, SplicingPathogenicityData> data = evaluator.evaluate(variant);

        // assert
        assertThat(data.size(), is(0));
    }

    @Test
    void evaluateVariantWhenReferenceSequenceIsNotAvailable() {
        // arrange mocks
        when(accessor.getReferenceDictionary())
                .thenReturn(referenceDictionary);
        when(accessor.fetchSequence(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 950, 2050)))
                .thenReturn(Optional.of(PojosForTesting.getSequenceIntervalForTranscriptWithThreeExons(referenceDictionary)));

        // empty transcript list
        when(transcriptSource.fetchTranscripts("chr1", 1199, 1200, referenceDictionary))
                .thenReturn(List.of());

        // act
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1199), "G", "A");
        final Map<String, SplicingPathogenicityData> data = evaluator.evaluate(variant);

        // assert
        assertThat(data.size(), is(0)); // empty map
    }

    @Test
    void evaluateVariantOnUnknownContig() {
        // arrange mocks
        when(accessor.getReferenceDictionary())
                .thenReturn(referenceDictionary);
        when(accessor.fetchSequence(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 950, 2050)))
                .thenReturn(Optional.of(PojosForTesting.getSequenceIntervalForTranscriptWithThreeExons(referenceDictionary)));

        when(transcriptSource.fetchTranscripts("chr1", 1199, 1200, referenceDictionary))
                .thenReturn(List.of(PojosForTesting.getTranscriptWithThreeExons(referenceDictionary)));

        // UNKNOWN contig
        final Map<String, SplicingPathogenicityData> data = evaluator.evaluate("UNKNOWN", 1000, "T", "C");
        assertThat(data.size(), is(0));
    }
}