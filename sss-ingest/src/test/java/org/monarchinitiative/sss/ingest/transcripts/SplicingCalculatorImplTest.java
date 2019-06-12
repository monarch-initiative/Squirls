package org.monarchinitiative.sss.ingest.transcripts;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.sss.core.model.GenomeCoordinates;
import org.monarchinitiative.sss.core.model.SequenceInterval;
import org.monarchinitiative.sss.core.model.SplicingTranscript;
import org.monarchinitiative.sss.core.pwm.SplicingInformationContentAnnotator;
import org.monarchinitiative.sss.core.reference.fasta.GenomeSequenceAccessor;
import org.monarchinitiative.sss.ingest.MakeSplicePositionWeightMatrices;
import org.monarchinitiative.sss.ingest.TestDataInstances;
import org.monarchinitiative.sss.ingest.TestDataSourceConfig;
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
    SplicingInformationContentAnnotator annotator;

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
                .thenReturn(SequenceInterval.newBuilder()
                        .setCoordinates(GenomeCoordinates.newBuilder()
                                .setContig("chr2")
                                .setBegin(9_900)
                                .setEnd(20_100)
                                .setStrand(true)
                                .build())
                        .setSequence(mockSeq)
                        .build());
    }

    @Test
    void singleExonTranscript() throws Exception {
        TranscriptModel tm = TestDataInstances.makeSingleExonTranscriptModel(referenceDictionary);
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
        TranscriptModel tm = TestDataInstances.makeThreeExonTranscriptModel(referenceDictionary);

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
                .thenReturn(SequenceInterval.newBuilder()
                        .setCoordinates(GenomeCoordinates.newBuilder()
                                .setContig("chr2")
                                .setBegin(0)
                                .setEnd(300)
                                .setStrand(true)
                                .build())
                        .setSequence(mockSeq)
                        .build());
        TranscriptModel tm = TestDataInstances.makeSmallTranscriptModel(referenceDictionary);

        Optional<SplicingTranscript> stOptional = instance.calculate(tm);

        assertThat(stOptional.isPresent(), is(true));

        SplicingTranscript st = stOptional.get();
        assertThat(st.getStrand(), is(true));
        assertThat(st.getTxBegin(), is(100));
        assertThat(st.getTxEnd(), is(200));
    }
}