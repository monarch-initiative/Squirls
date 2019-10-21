package org.monarchinitiative.threes.ingest.transcripts;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.model.GenomeCoordinates;
import org.monarchinitiative.threes.core.model.SequenceInterval;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.reference.fasta.GenomeSequenceAccessor;
import org.monarchinitiative.threes.ingest.MakeSplicePositionWeightMatrices;
import org.monarchinitiative.threes.ingest.PojosForTesting;
import org.monarchinitiative.threes.ingest.TestDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {TestDataSourceConfig.class})
class SplicingCalculatorImplTest {

    @Mock
    GenomeSequenceAccessor accessor;

    @Mock
    SplicingInformationContentCalculator annotator;

    private SplicingCalculatorImpl instance;

    @Autowired
    private ReferenceDictionary referenceDictionary;


    @BeforeEach
    void setUp() throws Exception {
        instance = new SplicingCalculatorImpl(accessor, annotator);

        char[] chars = new char[10_200];
        Arrays.fill(chars, 'A');
        String mockSeq = new String(chars);

        when(accessor.fetchSequence("chr2", 9_900, 20_100, true))
                .thenReturn(SequenceInterval.of(GenomeCoordinates.newBuilder()
                                .setContig("chr2")
                                .setBegin(9_900)
                                .setEnd(20_100)
                                .setStrand(true)
                                .build(),
                        mockSeq));
    }

    @Test
    void singleExonTranscript() throws Exception {
        TranscriptModel tm = PojosForTesting.makeSingleExonTranscriptModel(referenceDictionary);
        Optional<SplicingTranscript> stOptional = instance.calculate(tm);

        assertThat(stOptional.isPresent(), is(true));

        SplicingTranscript st = stOptional.get();
        assertThat(st.getAccessionId(), is("ACCID"));

        assertThat(st.getStrand(), is(true));
        assertThat(st.getTxBegin(), is(10_000));
        assertThat(st.getTxEnd(), is(20_000));
    }


    @Test
    void threeExonTranscript() {
        when(annotator.getSplicingParameters())
                .thenReturn(MakeSplicePositionWeightMatrices.makeSplicingParameters());
        TranscriptModel tm = PojosForTesting.makeThreeExonTranscriptModel(referenceDictionary);

        Optional<SplicingTranscript> stOptional = instance.calculate(tm);

        assertThat(stOptional.isPresent(), is(true));

        SplicingTranscript st = stOptional.get();
        assertThat(st.getAccessionId(), is("ACCID"));

        assertThat(st.getStrand(), is(true));
        assertThat(st.getTxBegin(), is(10_000));
        assertThat(st.getTxEnd(), is(20_000));
    }


    @Test
    void smallTranscript() throws Exception {
        when(annotator.getSplicingParameters())
                .thenReturn(MakeSplicePositionWeightMatrices.makeSplicingParameters());
        char[] chars = new char[300];
        Arrays.fill(chars, 'A');
        String mockSeq = new String(chars);
        when(accessor.fetchSequence("chr2", 0, 300, true))
                .thenReturn(SequenceInterval.of(GenomeCoordinates.newBuilder()
                                .setContig("chr2")
                                .setBegin(0)
                                .setEnd(300)
                                .setStrand(true)
                                .build(),
                        mockSeq));
        TranscriptModel tm = PojosForTesting.makeSmallTranscriptModel(referenceDictionary);

        Optional<SplicingTranscript> stOptional = instance.calculate(tm);

        assertThat(stOptional.isPresent(), is(true));

        SplicingTranscript st = stOptional.get();
        assertThat(st.getStrand(), is(true));
        assertThat(st.getTxBegin(), is(100));
        assertThat(st.getTxEnd(), is(200));
    }
}