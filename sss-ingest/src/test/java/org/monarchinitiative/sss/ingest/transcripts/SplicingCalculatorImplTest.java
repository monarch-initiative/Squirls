package org.monarchinitiative.sss.ingest.transcripts;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import de.charite.compbio.jannovar.reference.TranscriptModelBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.sss.core.model.SequenceInterval;
import org.monarchinitiative.sss.core.model.SplicingTranscript;
import org.monarchinitiative.sss.core.pwm.SplicingInformationContentAnnotator;
import org.monarchinitiative.sss.core.reference.GenomeSequenceAccessor;
import org.monarchinitiative.sss.ingest.MakeSplicePositionWeightMatrices;
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

    private static TranscriptModel makeThreeExonTranscriptModel(ReferenceDictionary referenceDictionary) {
        TranscriptModelBuilder builder = new TranscriptModelBuilder();
        builder.setAccession("ACCID");
        builder.setGeneSymbol("GENE");
        builder.setSequence("");

        GenomeInterval txRegion = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 10_000, 20_000);
        builder.setTXRegion(txRegion);

        GenomeInterval cdsRegion = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 11_000, 19_000);
        builder.setCDSRegion(cdsRegion);

        GenomeInterval first = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 10_000, 12_000);
        builder.addExonRegion(first);

        GenomeInterval second = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 14_000, 16_000);
        builder.addExonRegion(second);

        GenomeInterval third = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 18_000, 20_000);
        builder.addExonRegion(third);

        return builder.build();
    }

    private static TranscriptModel makeSingleExonTranscriptModel(ReferenceDictionary referenceDictionary) {
        TranscriptModelBuilder builder = new TranscriptModelBuilder();
        builder.setAccession("ACCID");
        builder.setGeneSymbol("GENE");
        builder.setSequence("");

        GenomeInterval txRegion = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 10_000, 20_000);
        builder.setTXRegion(txRegion);

        GenomeInterval cdsRegion = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 11_000, 19_000);
        builder.setCDSRegion(cdsRegion);

        GenomeInterval exon = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 10_000, 20_000);
        builder.addExonRegion(exon);
        return builder.build();
    }

    private static TranscriptModel makeSmallTranscriptModel(ReferenceDictionary referenceDictionary) {
        TranscriptModelBuilder builder = new TranscriptModelBuilder();
        builder.setAccession("ACCID");
        builder.setGeneSymbol("GENE");
        builder.setSequence("");

        GenomeInterval txRegion = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 100, 200);
        builder.setTXRegion(txRegion);

        GenomeInterval cdsRegion = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 120, 180);
        builder.setCDSRegion(cdsRegion);

        // the first and the last exons are really small
        builder.addExonRegion(new GenomeInterval(referenceDictionary, Strand.FWD, 2, 100, 101));
        builder.addExonRegion(new GenomeInterval(referenceDictionary, Strand.FWD, 2, 110, 160));
        builder.addExonRegion(new GenomeInterval(referenceDictionary, Strand.FWD, 2, 170, 190));
        builder.addExonRegion(new GenomeInterval(referenceDictionary, Strand.FWD, 2, 199, 200));
        return builder.build();
    }

    @BeforeEach
    void setUp() throws Exception {
        instance = new SplicingCalculatorImpl(accessor, annotator);

        char[] chars = new char[10_200];
        Arrays.fill(chars, 'A');
        String mockSeq = new String(chars);

        when(accessor.fetchSequence("chr2", 9_900, 20_100, true))
                .thenReturn(SequenceInterval.newBuilder()
                        .setInterval(org.monarchinitiative.sss.core.model.GenomeInterval.newBuilder()
                                .setContig("chr2")
                                .setBegin(9_900)
                                .setEnd(20_100)
                                .setStrand(true)
                                .setContigLength(100_000)
                                .build())
                        .setSequence(mockSeq)
                        .build());
    }

    @Test
    void singleExonTranscript() throws Exception {
        TranscriptModel tm = makeSingleExonTranscriptModel(referenceDictionary);
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
        TranscriptModel tm = makeThreeExonTranscriptModel(referenceDictionary);

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
                        .setInterval(org.monarchinitiative.sss.core.model.GenomeInterval.newBuilder()
                                .setContig("chr2")
                                .setBegin(0)
                                .setEnd(300)
                                .setStrand(true)
                                .setContigLength(100_000)
                                .build())
                        .setSequence(mockSeq)
                        .build());
        TranscriptModel tm = makeSmallTranscriptModel(referenceDictionary);

        Optional<SplicingTranscript> stOptional = instance.calculate(tm);

        assertThat(stOptional.isPresent(), is(true));

        SplicingTranscript st = stOptional.get();
        assertThat(st.getStrand(), is(true));
        assertThat(st.getTxBegin(), is(100));
        assertThat(st.getTxEnd(), is(200));
    }
}