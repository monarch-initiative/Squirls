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

    @BeforeEach
    void setUp() throws Exception {
        instance = new SplicingCalculatorImpl(accessor, annotator);

        char[] chars = new char[10_000];
        Arrays.fill(chars, 'A');
        String mockSeq = new String(chars);

        when(accessor.fetchSequence("chr2", 10_000, 20_000, true))
                .thenReturn(SequenceInterval.newBuilder()
                        .setInterval(org.monarchinitiative.sss.core.model.GenomeInterval.newBuilder()
                                .setContig("chr2")
                                .setBegin(10_000)
                                .setEnd(20_000)
                                .setStrand(true)
                                .setContigLength(100_000)
                                .build())
                        .setSequence(mockSeq)
                        .build());

        when(annotator.getSplicingParameters())
                .thenReturn(MakeSplicePositionWeightMatrices.makeSplicingParameters());
    }

    @Test
    void basic() throws Exception {
        TranscriptModel tm = makeSingleExonTranscriptModel();
        Optional<SplicingTranscript> stOptional = instance.calculate(tm);

        assertThat(stOptional.isPresent(), is(true));

        SplicingTranscript st = stOptional.get();
        assertThat(st.getAccessionId(), is("ACCID"));

        assertThat(st.getStrand(), is(true));
        assertThat(st.getTxBegin(), is(10_000));
        assertThat(st.getTxEnd(), is(20_000));
    }


    private TranscriptModel makeSingleExonTranscriptModel() {
        TranscriptModelBuilder builder = new TranscriptModelBuilder();
        builder.setAccession("ACCID");
        builder.setGeneSymbol("GENE");
        builder.setSequence("ACGT");

        GenomeInterval txRegion = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 10_000, 20_000);
        builder.setTXRegion(txRegion);

        GenomeInterval cdsRegion = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 11_000, 19_000);
        builder.setCDSRegion(cdsRegion);

        GenomeInterval exon = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 10_000, 20_000);
        builder.addExonRegion(exon);
        return builder.build();
    }
}