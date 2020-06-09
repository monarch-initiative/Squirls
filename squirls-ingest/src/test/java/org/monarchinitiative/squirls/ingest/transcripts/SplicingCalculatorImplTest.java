package org.monarchinitiative.squirls.ingest.transcripts;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.squirls.ingest.MakeSplicePositionWeightMatrices;
import org.monarchinitiative.squirls.ingest.PojosForTesting;
import org.monarchinitiative.squirls.ingest.TestDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessor;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

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
        final GenomeInterval gi = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 9_900, 20_100);
        when(accessor.fetchSequence(gi))
                .thenReturn(Optional.of(SequenceInterval.builder()
                        .interval(gi)
                        .sequence(mockSeq)
                        .build()));
    }

    @Test
    void singleExonTranscript() throws Exception {
        TranscriptModel tm = PojosForTesting.makeSingleExonTranscriptModel(referenceDictionary);
        Optional<SplicingTranscript> stOptional = instance.calculate(tm);

        assertThat(stOptional.isPresent(), is(true));

        SplicingTranscript st = stOptional.get();
        assertThat(st.getAccessionId(), is("ACCID"));

        assertThat(st.getStrand(), is(Strand.FWD));
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

        assertThat(st.getStrand(), is(Strand.FWD));
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
        final GenomeInterval gi = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 0, 300);
        when(accessor.fetchSequence(gi))
                .thenReturn(Optional.of(SequenceInterval.builder()
                        .interval(gi)
                        .sequence(mockSeq)
                        .build()));
        TranscriptModel tm = PojosForTesting.makeSmallTranscriptModel(referenceDictionary);

        Optional<SplicingTranscript> stOptional = instance.calculate(tm);

        assertThat(stOptional.isPresent(), is(true));

        SplicingTranscript st = stOptional.get();
        assertThat(st.getStrand(), is(Strand.FWD));
        assertThat(st.getTxBegin(), is(100));
        assertThat(st.getTxEnd(), is(200));
    }
}